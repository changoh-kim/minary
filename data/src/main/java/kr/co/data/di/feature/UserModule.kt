package kr.co.data.di.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.user.repository.UserDataSyncStateRepositoryImpl
import kr.co.data.feature.user.repository.UserStorageRepositoryImpl
import kr.co.domain.feature.user.repository.UserDataSyncStateRepository
import kr.co.domain.feature.user.repository.UserStorageRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface UserModule {

    @Binds
    @Singleton
    fun bindUserStorageRepository(impl: UserStorageRepositoryImpl): UserStorageRepository

    @Binds
    @Singleton
    fun bindUserDataSyncStateRepository(impl: UserDataSyncStateRepositoryImpl): UserDataSyncStateRepository
}