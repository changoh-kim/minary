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
import kr.co.domain.feature.diary.service.sync.DiarySyncManager
import kr.co.domain.feature.setting.repository.UserSettingsRepository

@HiltWorker
class DiaryImmediateSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val auth: FirebaseAuth,
    private val userSettingsRepository: UserSettingsRepository,
    private val diarySyncManager: DiarySyncManager,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "immediate_sync_worker"
    }

    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()

        val isSyncEnabled = userSettingsRepository.getDiarySyncEnabledStream().first()
        if (!isSyncEnabled) return Result.success()

        return diarySyncManager.performImmediatePush(userId)
            .fold(
                success = { Result.success() },
                failure = {
                    if (runAttemptCount < 3)
                        Result.retry()
                    else
                        Result.failure()
                }
            )
    }
}