package kr.co.domain.service.remoteconfig.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.co.domain.service.remoteconfig.model.ServiceStatus
import kr.co.domain.service.remoteconfig.repository.RemoteConfigRepository
import kr.co.domain.testing.DomainCoroutineTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class CheckServiceStatusUseCaseTest : DomainCoroutineTest() {

    private val repository = mockk<RemoteConfigRepository>()
    private val useCase = CheckServiceStatusUseCase(repository)

    @Test
    fun `returns active status after fetch when maintenance mode is disabled`() {
        runDomainTest {
            coEvery { repository.fetchAndActivate() } returns true
            every { repository.isMaintenanceMode() } returns false

            val result = useCase()

            assertSame(ServiceStatus.Active, result)
            coVerify(exactly = 1) { repository.fetchAndActivate() }
            verify(exactly = 1) { repository.isMaintenanceMode() }
            verify(exactly = 0) { repository.getMaintenanceReason() }
        }
    }

    @Test
    fun `returns maintenance status with reason after fetch when maintenance mode is enabled`() {
        runDomainTest {
            coEvery { repository.fetchAndActivate() } returns true
            every { repository.isMaintenanceMode() } returns true
            every { repository.getMaintenanceReason() } returns "reason-test"

            val result = useCase()

            assertEquals(ServiceStatus.Maintenance("reason-test"), result)
            coVerify(exactly = 1) { repository.fetchAndActivate() }
            verify(exactly = 1) { repository.isMaintenanceMode() }
            verify(exactly = 1) { repository.getMaintenanceReason() }
        }
    }
}
