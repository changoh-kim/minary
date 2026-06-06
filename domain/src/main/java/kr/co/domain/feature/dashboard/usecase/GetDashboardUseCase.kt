package kr.co.domain.feature.dashboard.usecase

import com.github.michaelbull.result.Result
import kr.co.domain.error.DomainError
import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.domain.feature.dashboard.repository.DashboardRepository
import javax.inject.Inject


class GetDashboardUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository,
) {
    suspend operator fun invoke(): Result<Dashboard, DomainError> {
        return dashboardRepository.getDashboard()
    }
}