package kr.co.data.di.service

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.service.remoteconfig.RemoteConfigRepositoryImpl
import kr.co.domain.service.remoteconfig.repository.RemoteConfigRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RemoteConfigModule {

    @Binds
    @Singleton
    fun bindRemoteConfigRepository(impl: RemoteConfigRepositoryImpl): RemoteConfigRepository
}