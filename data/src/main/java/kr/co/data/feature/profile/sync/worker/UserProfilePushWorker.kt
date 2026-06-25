package kr.co.data.feature.profile.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.michaelbull.result.fold
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.domain.feature.profile.sync.UserProfileSyncManager

@HiltWorker
class UserProfilePushWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val firebaseAuthProvider: FirebaseAuthProvider,
    private val userProfileSyncManager: UserProfileSyncManager,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "user_profile_push_worker"
    }

    override suspend fun doWork(): Result {
        val userId = firebaseAuthProvider.currentUser?.uid ?: return Result.failure()

        return userProfileSyncManager.pushProfile(userId)
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
