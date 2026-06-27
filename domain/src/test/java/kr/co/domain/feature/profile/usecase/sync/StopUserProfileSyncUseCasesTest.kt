package kr.co.domain.feature.profile.usecase.sync

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import kr.co.domain.feature.profile.sync.UserProfileRealtimeSyncScheduler
import kr.co.domain.feature.profile.sync.UserProfileSyncScheduler
import org.junit.jupiter.api.Test

class StopUserProfileSyncUseCasesTest {

    private val scheduler = mockk<UserProfileSyncScheduler>()
    private val realtimeScheduler = mockk<UserProfileRealtimeSyncScheduler>()

    @Test
    fun `stops realtime user profile sync`() {
        every { realtimeScheduler.stopListening() } just runs
        val useCase = StopRealtimeUserProfileSyncUseCase(realtimeScheduler)

        useCase()

        verify(exactly = 1) { realtimeScheduler.stopListening() }
    }

    @Test
    fun `cancels all user profile sync then stops realtime sync`() {
        every { scheduler.cancelAll() } just runs
        every { realtimeScheduler.stopListening() } just runs
        val stopRealtime = StopRealtimeUserProfileSyncUseCase(realtimeScheduler)
        val useCase = StopAllUserProfileSyncUseCase(scheduler, stopRealtime)

        useCase()

        verifyOrder {
            scheduler.cancelAll()
            realtimeScheduler.stopListening()
        }
    }
}
