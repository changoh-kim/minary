package kr.co.data.di.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.profile.repository.UserProfileRepositoryImpl
import kr.co.data.feature.profile.sync.FirestoreUserProfileRealtimeSyncManager
import kr.co.data.feature.profile.sync.FirestoreUserProfileSyncManager
import kr.co.data.feature.profile.sync.UserProfileSyncSchedulerWorkManager
import kr.co.domain.feature.profile.repository.UserProfileRepository
import kr.co.domain.feature.profile.sync.UserProfileRealtimeSyncScheduler
import kr.co.domain.feature.profile.sync.UserProfileSyncManager
import kr.co.domain.feature.profile.sync.UserProfileSyncScheduler
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