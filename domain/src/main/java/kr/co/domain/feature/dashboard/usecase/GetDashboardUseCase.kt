package kr.co.domain.feature.dashboard.usecase

import kr.co.core.common.result.AppResult
import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.domain.feature.dashboard.repository.DashboardRepository
import javax.inject.Inject


class GetDashboardUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository,
) {
    suspend operator fun invoke(): AppResult<Dashboard> {
        return dashboardRepository.getDashboard()
    }
}