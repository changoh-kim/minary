package kr.co.domain.feature.time.usecase

import com.github.michaelbull.result.Err
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kr.co.core.common.error.DomainError
import kr.co.domain.service.time.ServerTimeSyncScheduler
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.FakeServerTimeProvider
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import org.junit.jupiter.api.Test

class SyncServerTimeUseCaseTest : DomainCoroutineTest() {

    private val scheduler = mockk<ServerTimeSyncScheduler>()

    @Test
    fun `does not schedule sync when server time sync succeeds`() {
        runDomainTest {
            val serverTime = FakeServerTimeProvider()
            val useCase = SyncServerTimeUseCase(serverTime, scheduler)

            val result = useCase()

            result.assertOk(Unit)
            verify(exactly = 0) { scheduler.scheduleSync() }
        }
    }

    @Test
    fun `schedules sync when server time sync fails`() {
        runDomainTest {
            val error = DomainError.NetworkUnavailable
            val serverTime = FakeServerTimeProvider(syncResult = Err(error))
            every { scheduler.scheduleSync() } just runs
            val useCase = SyncServerTimeUseCase(serverTime, scheduler)

            val result = useCase()

            result.assertErr(error)
            verify(exactly = 1) { scheduler.scheduleSync() }
        }
    }
}
