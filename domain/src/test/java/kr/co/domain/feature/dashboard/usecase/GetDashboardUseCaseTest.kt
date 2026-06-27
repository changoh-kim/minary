package kr.co.domain.feature.dashboard.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeDashboardRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetDashboardUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `returns dashboard from repository`() {
        runDomainTest {
            val dashboard = Dashboard(totalDiaryCount = 3)
            val repository = FakeDashboardRepository(dashboardResult = Ok(dashboard))
            val useCase = GetDashboardUseCase(repository)

            val result = useCase()

            result.assertOk(dashboard)
            assertEquals(1, repository.getDashboardCallCount)
        }
    }

    @Test
    fun `returns repository failure`() {
        runDomainTest {
            val error = DomainError.Store.NotFound
            val repository = FakeDashboardRepository(dashboardResult = Err(error))
            val useCase = GetDashboardUseCase(repository)

            val result = useCase()

            result.assertErr(error)
            assertEquals(1, repository.getDashboardCallCount)
        }
    }
}
