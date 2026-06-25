package kr.co.data.di.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.service.time.TrueTimeProvider
import kr.co.data.service.time.sync.ServerTimeSyncWorkManager
import kr.co.domain.service.time.ServerTimeProvider
import kr.co.domain.service.time.ServerTimeSyncScheduler
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