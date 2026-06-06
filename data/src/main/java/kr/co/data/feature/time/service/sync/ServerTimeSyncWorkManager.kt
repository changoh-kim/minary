package kr.co.data.feature.time.service.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.data.feature.time.service.sync.worker.ServerTimeSyncWorker
import kr.co.domain.feature.time.service.ServerTimeSyncScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerTimeSyncWorkManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ServerTimeSyncScheduler {

    private val workManager get() = WorkManager.getInstance(context)

    override fun scheduleSync() {
        val request =
            OneTimeWorkRequestBuilder<ServerTimeSyncWorker>()
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniqueWork(
            ServerTimeSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
    override fun cancelSync() {
        workManager.cancelUniqueWork(ServerTimeSyncWorker.WORK_NAME)
    }
}