package kr.co.data.di.module.infra

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.remote.firebase.repository.RemoteConfigRepositoryImpl
import kr.co.domain.infra.remote.repository.RemoteConfigRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RemoteConfigModule {

    @Binds
    @Singleton
    fun bindRemoteConfigRepository(impl: RemoteConfigRepositoryImpl): RemoteConfigRepository
}