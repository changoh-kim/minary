package kr.co.data.di.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.dashboard.repository.DashboardRepositoryImpl
import kr.co.domain.feature.dashboard.repository.DashboardRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DashboardModule {
    @Binds
    @Singleton
    fun bindDashBoardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}