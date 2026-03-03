package kr.co.domain.feature.dashboard.repository

import kr.co.domain.feature.dashboard.model.Dashboard


interface DashboardRepository {
    suspend fun getDashboard(): Result<Dashboard>
}