package kr.co.data.feature.setting.service.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.michaelbull.result.fold
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kr.co.domain.feature.setting.service.sync.UserSettingsSyncManager

@HiltWorker
class UserSettingsPushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val auth: FirebaseAuth,
    private val userSettingsSyncManager: UserSettingsSyncManager,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "user_settings_push_worker"
    }

    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()

        return userSettingsSyncManager.pushSettings(userId)
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