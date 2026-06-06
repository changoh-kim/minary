package kr.co.data.di.module.infra

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.infra.service.network.NetworkMonitorImpl
import kr.co.domain.infra.network.service.NetworkMonitor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NetworkModule {

    @Binds
    @Singleton
    fun bindNetworkMonitor(impl: NetworkMonitorImpl): NetworkMonitor
}