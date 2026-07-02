package kr.co.data.feature.profile.sync

import androidx.work.BackoffPolicy
import androidx.work.NetworkType
import androidx.work.WorkInfo
import kr.co.data.feature.profile.sync.worker.UserProfilePhotoPushWorker
import kr.co.data.feature.profile.sync.worker.UserProfilePushWorker
import kr.co.data.testing.BaseWorkManagerSchedulerTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.concurrent.TimeUnit

class UserProfileSyncSchedulerWorkManagerInstrumentedTest : BaseWorkManagerSchedulerTest() {
    private val scheduler: UserProfileSyncSchedulerWorkManager
        get() = UserProfileSyncSchedulerWorkManager(context)

    @Test
    fun scheduleProfilePush_enqueues_connected_one_time_profile_push_work() {
        scheduler.scheduleProfilePush()

        val spec = onlyWorkSpecForUniqueWork(UserProfilePushWorker.WORK_NAME)
        assertEquals(UserProfilePushWorker::class.java.name, spec.workerClassName)
        assertEquals(WorkInfo.State.ENQUEUED, spec.state)
        assertFalse(spec.isPeriodic)
        assertEquals(NetworkType.CONNECTED, spec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, spec.backoffPolicy)
        assertEquals(TimeUnit.SECONDS.toMillis(10), spec.backoffDelayDuration)
    }

    @Test
    fun scheduleProfilePhotoPush_enqueues_connected_one_time_photo_push_work() {
        scheduler.scheduleProfilePhotoPush()

        val spec = onlyWorkSpecForUniqueWork(UserProfilePhotoPushWorker.WORK_NAME)
        assertEquals(UserProfilePhotoPushWorker::class.java.name, spec.workerClassName)
        assertEquals(WorkInfo.State.ENQUEUED, spec.state)
        assertFalse(spec.isPeriodic)
        assertEquals(NetworkType.CONNECTED, spec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, spec.backoffPolicy)
        assertEquals(TimeUnit.SECONDS.toMillis(10), spec.backoffDelayDuration)
    }

    @Test
    fun scheduleProfilePush_keeps_existing_profile_push_work() {
        scheduler.scheduleProfilePush()
        scheduler.scheduleProfilePush()

        assertEquals(1, workSpecsForUniqueWork(UserProfilePushWorker.WORK_NAME).size)
    }

    @Test
    fun cancelAll_cancels_profile_and_photo_push_work() {
        scheduler.scheduleProfilePush()
        scheduler.scheduleProfilePhotoPush()

        scheduler.cancelAll()

        assertUniqueWorkCancelled(UserProfilePushWorker.WORK_NAME)
        assertUniqueWorkCancelled(UserProfilePhotoPushWorker.WORK_NAME)
    }
}
