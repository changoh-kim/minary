package kr.co.domain.testing.fake

import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.domain.feature.dashboard.repository.DashboardRepository

class FakeDashboardRepository(
    var dashboardResult: AppResult<Dashboard> = Ok(Dashboard()),
) : DashboardRepository {
    var getDashboardCallCount = 0
        private set

    override suspend fun getDashboard(): AppResult<Dashboard> {
        getDashboardCallCount += 1
        return dashboardResult
    }
}
