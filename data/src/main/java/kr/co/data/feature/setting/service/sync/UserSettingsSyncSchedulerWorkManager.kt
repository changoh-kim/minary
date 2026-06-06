package kr.co.data.feature.setting.service.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.data.feature.setting.service.sync.worker.UserSettingsPushWorker
import kr.co.domain.feature.setting.service.sync.UserSettingsSyncScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsSyncSchedulerWorkManager @Inject constructor(
    @param:ApplicationContext private val context: Context
): UserSettingsSyncScheduler {

    private val workManager get() = WorkManager.getInstance(context)

    override fun scheduleSettingsPush() {
        val request =
            OneTimeWorkRequestBuilder<UserSettingsPushWorker>()
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()

        workManager.enqueueUniqueWork(
            UserSettingsPushWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun cancelSettingsPush() {
        workManager.cancelUniqueWork(UserSettingsPushWorker.WORK_NAME)
    }

    override fun cancelAll() {
        workManager.cancelUniqueWork(UserSettingsPushWorker.WORK_NAME)
    }
}