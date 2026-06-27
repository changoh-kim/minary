package kr.co.domain.feature.setting.usecase.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.co.domain.feature.setting.sync.UserSettingsRealtimeSyncScheduler
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.assertOk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class StartRealtimeUserSettingsSyncUseCaseTest : DomainCoroutineTest() {

    private val scheduler = mockk<UserSettingsRealtimeSyncScheduler>()
    private val useCase = StartRealtimeUserSettingsSyncUseCase(scheduler)

    @Test
    fun `starts realtime user settings sync and returns ok`() {
        runDomainTest {
            coEvery { scheduler.startListening() } just runs

            val result = useCase()

            result.assertOk(Unit)
            coVerify(exactly = 1) { scheduler.startListening() }
        }
    }

    @Test
    fun `propagates start listening exception`() {
        val exception = IllegalStateException("failure-test")
        coEvery { scheduler.startListening() } throws exception

        val actual = assertThrows(IllegalStateException::class.java) {
            runDomainTest {
                useCase()
            }
        }

        assertEquals(exception, actual)
    }
}
