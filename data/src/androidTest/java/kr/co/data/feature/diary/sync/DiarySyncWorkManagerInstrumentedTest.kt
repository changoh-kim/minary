package kr.co.data.feature.diary.sync

import androidx.work.BackoffPolicy
import androidx.work.NetworkType
import androidx.work.WorkInfo
import kr.co.data.feature.diary.sync.worker.DiaryFullSyncWorker
import kr.co.data.feature.diary.sync.worker.DiaryImmediateSyncWorker
import kr.co.data.feature.diary.sync.worker.DiaryPeriodicSyncWorker
import kr.co.data.testing.BaseWorkManagerSchedulerTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class DiarySyncWorkManagerInstrumentedTest : BaseWorkManagerSchedulerTest() {
    private val scheduler: DiarySyncWorkManager
        get() = DiarySyncWorkManager(context)

    @Test
    fun scheduleFullSync_enqueues_connected_one_time_full_sync_work() {
        scheduler.scheduleFullSync()

        val spec = onlyWorkSpecForUniqueWork(DiaryFullSyncWorker.WORK_NAME)
        assertEquals(DiaryFullSyncWorker::class.java.name, spec.workerClassName)
        assertEquals(WorkInfo.State.ENQUEUED, spec.state)
        assertFalse(spec.isPeriodic)
        assertEquals(NetworkType.CONNECTED, spec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, spec.backoffPolicy)
        assertEquals(TimeUnit.SECONDS.toMillis(15), spec.backoffDelayDuration)
    }

    @Test
    fun scheduleFullSync_keeps_existing_full_sync_work() {
        scheduler.scheduleFullSync()
        scheduler.scheduleFullSync()

        assertEquals(1, workSpecsForUniqueWork(DiaryFullSyncWorker.WORK_NAME).size)
    }

    @Test
    fun rescheduleFullSync_appends_new_full_sync_work_when_existing_work_is_active() {
        scheduler.scheduleFullSync()
        scheduler.rescheduleFullSync()

        val specs = workSpecsForUniqueWork(DiaryFullSyncWorker.WORK_NAME)
        assertEquals(2, specs.size)
        assertTrue(specs.all { it.workerClassName == DiaryFullSyncWorker::class.java.name })
    }

    @Test
    fun scheduleImmediateSync_enqueues_connected_one_time_immediate_sync_work() {
        scheduler.scheduleImmediateSync()

        val spec = onlyWorkSpecForUniqueWork(DiaryImmediateSyncWorker.WORK_NAME)
        assertEquals(DiaryImmediateSyncWorker::class.java.name, spec.workerClassName)
        assertEquals(WorkInfo.State.ENQUEUED, spec.state)
        assertFalse(spec.isPeriodic)
        assertEquals(NetworkType.CONNECTED, spec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, spec.backoffPolicy)
        assertEquals(TimeUnit.SECONDS.toMillis(10), spec.backoffDelayDuration)
    }

    @Test
    fun scheduleImmediateSync_keeps_existing_immediate_sync_work() {
        scheduler.scheduleImmediateSync()
        scheduler.scheduleImmediateSync()

        assertEquals(1, workSpecsForUniqueWork(DiaryImmediateSyncWorker.WORK_NAME).size)
    }

    @Test
    fun schedulePeriodicSync_enqueues_unmetered_charging_idle_periodic_work() {
        scheduler.schedulePeriodicSync()

        val spec = onlyWorkSpecForUniqueWork(DiaryPeriodicSyncWorker.WORK_NAME)
        assertEquals(DiaryPeriodicSyncWorker::class.java.name, spec.workerClassName)
        assertEquals(WorkInfo.State.ENQUEUED, spec.state)
        assertTrue(spec.isPeriodic)
        assertEquals(TimeUnit.HOURS.toMillis(24), spec.intervalDuration)
        assertEquals(NetworkType.UNMETERED, spec.constraints.requiredNetworkType)
        assertTrue(spec.constraints.requiresCharging())
        assertTrue(spec.constraints.requiresDeviceIdle())
    }

    @Test
    fun schedulePeriodicSync_keeps_existing_periodic_sync_work() {
        scheduler.schedulePeriodicSync()
        scheduler.schedulePeriodicSync()

        assertEquals(1, workSpecsForUniqueWork(DiaryPeriodicSyncWorker.WORK_NAME).size)
    }

    @Test
    fun cancelAllSync_cancels_full_immediate_and_periodic_work() {
        scheduler.scheduleFullSync()
        scheduler.scheduleImmediateSync()
        scheduler.schedulePeriodicSync()

        scheduler.cancelAllSync()

        assertUniqueWorkCancelled(DiaryFullSyncWorker.WORK_NAME)
        assertUniqueWorkCancelled(DiaryImmediateSyncWorker.WORK_NAME)
        assertUniqueWorkCancelled(DiaryPeriodicSyncWorker.WORK_NAME)
    }
}
