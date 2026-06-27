package kr.co.domain.feature.diary.usecase.sync

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.co.domain.feature.diary.sync.DiaryRealtimeSyncManager
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.assertOk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class StartRealtimeDiarySyncUseCaseTest : DomainCoroutineTest() {

    private val realtimeSyncManager = mockk<DiaryRealtimeSyncManager>()
    private val useCase = StartRealtimeDiarySyncUseCase(realtimeSyncManager)

    @Test
    fun `starts realtime diary sync and returns ok`() {
        runDomainTest {
            coEvery { realtimeSyncManager.startListening() } just runs

            val result = useCase()

            result.assertOk(Unit)
            coVerify(exactly = 1) { realtimeSyncManager.startListening() }
        }
    }

    @Test
    fun `propagates start listening exception`() {
        val exception = IllegalStateException("failure-test")
        coEvery { realtimeSyncManager.startListening() } throws exception

        val actual = assertThrows(IllegalStateException::class.java) {
            runDomainTest {
                useCase()
            }
        }

        assertEquals(exception, actual)
    }
}
