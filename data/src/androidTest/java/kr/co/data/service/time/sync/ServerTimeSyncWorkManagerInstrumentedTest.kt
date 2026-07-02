package kr.co.data.service.time.sync

import androidx.work.BackoffPolicy
import androidx.work.NetworkType
import androidx.work.WorkInfo
import kr.co.data.service.time.sync.worker.ServerTimeSyncWorker
import kr.co.data.testing.BaseWorkManagerSchedulerTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.concurrent.TimeUnit

class ServerTimeSyncWorkManagerInstrumentedTest : BaseWorkManagerSchedulerTest() {
    private val scheduler: ServerTimeSyncWorkManager
        get() = ServerTimeSyncWorkManager(context)

    @Test
    fun scheduleSync_enqueues_connected_one_time_server_time_sync_work() {
        scheduler.scheduleSync()

        val spec = onlyWorkSpecForUniqueWork(ServerTimeSyncWorker.WORK_NAME)
        assertEquals(ServerTimeSyncWorker::class.java.name, spec.workerClassName)
        assertEquals(WorkInfo.State.ENQUEUED, spec.state)
        assertFalse(spec.isPeriodic)
        assertEquals(NetworkType.CONNECTED, spec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, spec.backoffPolicy)
        assertEquals(TimeUnit.MINUTES.toMillis(1), spec.backoffDelayDuration)
    }

    @Test
    fun scheduleSync_keeps_existing_server_time_sync_work() {
        scheduler.scheduleSync()
        scheduler.scheduleSync()

        assertEquals(1, workSpecsForUniqueWork(ServerTimeSyncWorker.WORK_NAME).size)
    }

    @Test
    fun cancelSync_cancels_server_time_sync_work() {
        scheduler.scheduleSync()

        scheduler.cancelSync()

        assertUniqueWorkCancelled(ServerTimeSyncWorker.WORK_NAME)
    }
}
