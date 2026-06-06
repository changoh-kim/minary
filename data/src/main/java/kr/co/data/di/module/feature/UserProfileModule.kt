package kr.co.data.di.module.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.profile.repository.UserProfileRepositoryImpl
import kr.co.data.feature.profile.service.sync.FirestoreUserProfileRealtimeSyncManager
import kr.co.data.feature.profile.service.sync.FirestoreUserProfileSyncManager
import kr.co.data.feature.profile.service.sync.UserProfileSyncSchedulerWorkManager
import kr.co.domain.feature.profile.repository.UserProfileRepository
import kr.co.domain.feature.profile.service.sync.UserProfileRealtimeSyncScheduler
import kr.co.domain.feature.profile.service.sync.UserProfileSyncManager
import kr.co.domain.feature.profile.service.sync.UserProfileSyncScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface UserProfileModule {
    // repository
    @Binds
    @Singleton
    fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    // service
    @Binds
    @Singleton
    fun bindUserProfileSyncScheduler(impl: UserProfileSyncSchedulerWorkManager): UserProfileSyncScheduler

    @Binds
    @Singleton
    fun bindUserProfileSyncManager(impl: FirestoreUserProfileSyncManager): UserProfileSyncManager

    @Binds
    @Singleton
    fun bindUserProfileRealtimeSyncScheduler(impl: FirestoreUserProfileRealtimeSyncManager): UserProfileRealtimeSyncScheduler
}