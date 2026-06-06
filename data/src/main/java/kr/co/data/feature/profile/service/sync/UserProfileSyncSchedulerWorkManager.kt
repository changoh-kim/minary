package kr.co.data.feature.profile.service.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.data.feature.profile.service.sync.worker.UserProfilePhotoPushWorker
import kr.co.data.feature.profile.service.sync.worker.UserProfilePushWorker
import kr.co.domain.feature.profile.service.sync.UserProfileSyncScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileSyncSchedulerWorkManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) : UserProfileSyncScheduler {

    private val workManager get() = WorkManager.getInstance(context)

    override fun scheduleProfilePush() {
        val request =
            OneTimeWorkRequestBuilder<UserProfilePushWorker>()
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

        workManager.enqueueUniqueWork(
            UserProfilePushWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun scheduleProfilePhotoPush() {
        val request =
            OneTimeWorkRequestBuilder<UserProfilePhotoPushWorker>()
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

        workManager.enqueueUniqueWork(
            UserProfilePushWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun cancelProfilePush() {
        workManager.cancelUniqueWork(UserProfilePushWorker.WORK_NAME)
    }

    override fun cancelProfilePhotoPush() {
        workManager.cancelUniqueWork(UserProfilePhotoPushWorker.WORK_NAME)
    }

    override fun cancelAll() {
        workManager.cancelUniqueWork(UserProfilePushWorker.WORK_NAME)
        workManager.cancelUniqueWork(UserProfilePhotoPushWorker.WORK_NAME)
    }
}