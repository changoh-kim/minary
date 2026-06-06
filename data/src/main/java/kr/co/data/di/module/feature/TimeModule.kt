package kr.co.data.di.module.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.time.service.TrueTimeProvider
import kr.co.data.feature.time.service.sync.ServerTimeSyncWorkManager
import kr.co.domain.feature.time.service.ServerTimeProvider
import kr.co.domain.feature.time.service.ServerTimeSyncScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface TimeModule {
    @Binds
    @Singleton
    fun bindServerTimeProvider(impl: TrueTimeProvider): ServerTimeProvider

    @Binds
    @Singleton
    fun bindServerTimeSyncScheduler(impl: ServerTimeSyncWorkManager): ServerTimeSyncScheduler
}