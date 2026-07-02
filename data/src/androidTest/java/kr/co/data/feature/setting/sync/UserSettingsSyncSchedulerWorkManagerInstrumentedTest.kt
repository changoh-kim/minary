package kr.co.data.feature.setting.sync

import androidx.work.BackoffPolicy
import androidx.work.NetworkType
import androidx.work.WorkInfo
import kr.co.data.feature.setting.sync.worker.UserSettingsPushWorker
import kr.co.data.testing.BaseWorkManagerSchedulerTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.concurrent.TimeUnit

class UserSettingsSyncSchedulerWorkManagerInstrumentedTest : BaseWorkManagerSchedulerTest() {
    private val scheduler: UserSettingsSyncSchedulerWorkManager
        get() = UserSettingsSyncSchedulerWorkManager(context)

    @Test
    fun scheduleSettingsPush_enqueues_connected_one_time_settings_push_work() {
        scheduler.scheduleSettingsPush()

        val spec = onlyWorkSpecForUniqueWork(UserSettingsPushWorker.WORK_NAME)
        assertEquals(UserSettingsPushWorker::class.java.name, spec.workerClassName)
        assertEquals(WorkInfo.State.ENQUEUED, spec.state)
        assertFalse(spec.isPeriodic)
        assertEquals(NetworkType.CONNECTED, spec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, spec.backoffPolicy)
        assertEquals(TimeUnit.SECONDS.toMillis(10), spec.backoffDelayDuration)
    }

    @Test
    fun scheduleSettingsPush_keeps_existing_settings_push_work() {
        scheduler.scheduleSettingsPush()
        scheduler.scheduleSettingsPush()

        assertEquals(1, workSpecsForUniqueWork(UserSettingsPushWorker.WORK_NAME).size)
    }

    @Test
    fun cancelAll_cancels_settings_push_work() {
        scheduler.scheduleSettingsPush()

        scheduler.cancelAll()

        assertUniqueWorkCancelled(UserSettingsPushWorker.WORK_NAME)
    }
}
