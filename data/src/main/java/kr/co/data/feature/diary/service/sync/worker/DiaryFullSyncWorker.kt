package kr.co.data.feature.diary.service.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.michaelbull.result.fold
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.diary.service.sync.DiarySyncManager
import kr.co.domain.feature.diary.service.sync.DiarySyncScheduler
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.feature.time.service.ServerTimeProvider

@HiltWorker
class DiaryFullSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val auth: FirebaseAuth,
    private val serverTime: ServerTimeProvider,
    private val diaryRepository: DiaryRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val diarySyncManager: DiarySyncManager,
    private val diarySyncScheduler: DiarySyncScheduler,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "full_sync_worker"
    }

    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()

        serverTime.sync()

        val isSyncEnabled = userSettingsRepository.getDiarySyncEnabledStream().first()
        if (!isSyncEnabled) return Result.success()

        return diarySyncManager.performChunkedSync(userId)
            .fold(
                success = { hasMore ->
                    if (hasMore) {
                        diarySyncScheduler.rescheduleFullSync()
                    } else {
                        diaryRepository.deleteOldDiaries()
                    }
                    Result.success()
                },
                failure = {
                    if (runAttemptCount < 3)
                        Result.retry()
                    else
                        Result.failure()
                }
            )
    }
}