package kr.co.data.feature.diary.service.sync

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.data.feature.diary.service.sync.worker.DiaryFullSyncWorker
import kr.co.data.feature.diary.service.sync.worker.DiaryImmediateSyncWorker
import kr.co.data.feature.diary.service.sync.worker.DiaryPeriodicSyncWorker
import kr.co.domain.feature.diary.service.sync.DiarySyncScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiarySyncWorkManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DiarySyncScheduler {
    private val workManager get() = WorkManager.getInstance(context)

    override fun scheduleFullSync() {
        enqueueFullSync(ExistingWorkPolicy.KEEP)
    }

    override fun rescheduleFullSync() {
        enqueueFullSync(ExistingWorkPolicy.APPEND_OR_REPLACE)
    }

    private fun enqueueFullSync(existingWorkPolicy: ExistingWorkPolicy) {
        val request =
            OneTimeWorkRequestBuilder<DiaryFullSyncWorker>()
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()

        workManager.enqueueUniqueWork(
            DiaryFullSyncWorker.WORK_NAME,
            existingWorkPolicy,
            request
        )
    }

    override fun scheduleImmediateSync() {
        val request =
            OneTimeWorkRequestBuilder<DiaryImmediateSyncWorker>()
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

        workManager.enqueueUniqueWork(
            DiaryImmediateSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    @SuppressLint("IdleBatteryChargingConstraints")
    override fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .setRequiresDeviceIdle(true)
            .build()

        val request =
            PeriodicWorkRequestBuilder<DiaryPeriodicSyncWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DiaryPeriodicSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun cancelFullSync() {
        workManager.cancelUniqueWork(DiaryFullSyncWorker.WORK_NAME)
    }

    override fun cancelImmediateSync() {
        workManager.cancelUniqueWork(DiaryImmediateSyncWorker.WORK_NAME)
    }

    override fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(DiaryPeriodicSyncWorker.WORK_NAME)
    }

    override fun cancelAllSync() {
        workManager.cancelUniqueWork(DiaryFullSyncWorker.WORK_NAME)
        workManager.cancelUniqueWork(DiaryImmediateSyncWorker.WORK_NAME)
        workManager.cancelUniqueWork(DiaryPeriodicSyncWorker.WORK_NAME)
    }
}