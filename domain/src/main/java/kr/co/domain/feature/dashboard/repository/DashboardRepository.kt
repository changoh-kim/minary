package kr.co.domain.feature.dashboard.repository

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.dashboard.model.Dashboard


interface DashboardRepository {
    suspend fun getDashboard(): Result<Dashboard, DomainError>
}