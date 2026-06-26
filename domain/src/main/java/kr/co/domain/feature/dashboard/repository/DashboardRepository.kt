package kr.co.domain.feature.dashboard.repository

import kr.co.core.common.result.AppResult
import kr.co.domain.feature.dashboard.model.Dashboard


interface DashboardRepository {
    suspend fun getDashboard(): AppResult<Dashboard>
}