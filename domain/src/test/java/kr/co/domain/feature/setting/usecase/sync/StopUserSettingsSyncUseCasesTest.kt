package kr.co.domain.feature.setting.usecase.sync

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import kr.co.domain.feature.setting.sync.UserSettingsRealtimeSyncScheduler
import kr.co.domain.feature.setting.sync.UserSettingsSyncScheduler
import org.junit.jupiter.api.Test

class StopUserSettingsSyncUseCasesTest {

    private val scheduler = mockk<UserSettingsSyncScheduler>()
    private val realtimeScheduler = mockk<UserSettingsRealtimeSyncScheduler>()

    @Test
    fun `stops realtime user settings sync`() {
        every { realtimeScheduler.stopListening() } just runs
        val useCase = StopRealtimeUserSettingsSyncUseCase(realtimeScheduler)

        useCase()

        verify(exactly = 1) { realtimeScheduler.stopListening() }
    }

    @Test
    fun `cancels all user settings sync then stops realtime sync`() {
        every { scheduler.cancelAll() } just runs
        every { realtimeScheduler.stopListening() } just runs
        val stopRealtime = StopRealtimeUserSettingsSyncUseCase(realtimeScheduler)
        val useCase = StopAllUserSettingsSyncUseCase(scheduler, stopRealtime)

        useCase()

        verifyOrder {
            scheduler.cancelAll()
            realtimeScheduler.stopListening()
        }
    }
}
