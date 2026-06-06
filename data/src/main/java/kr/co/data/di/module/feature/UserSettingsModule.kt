package kr.co.data.di.module.feature

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.data.feature.setting.repository.UserSettingsRepositoryImpl
import kr.co.data.feature.setting.service.sync.FirestoreUserSettingsRealtimeSyncManager
import kr.co.data.feature.setting.service.sync.FirestoreUserSettingsSyncManager
import kr.co.data.feature.setting.service.sync.UserSettingsSyncSchedulerWorkManager
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.feature.setting.service.sync.UserSettingsRealtimeSyncScheduler
import kr.co.domain.feature.setting.service.sync.UserSettingsSyncManager
import kr.co.domain.feature.setting.service.sync.UserSettingsSyncScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface UserSettingsModule {
    // repository
    @Binds
    @Singleton
    fun bindUserSettingsRepository(impl: UserSettingsRepositoryImpl): UserSettingsRepository

    // service
    @Binds
    @Singleton
    fun bindUserSettingsSyncScheduler(impl: UserSettingsSyncSchedulerWorkManager): UserSettingsSyncScheduler

    @Binds
    @Singleton
    fun bindUserSettingsSyncManager(impl: FirestoreUserSettingsSyncManager): UserSettingsSyncManager

    @Binds
    @Singleton
    fun bindUserSettingsRealtimeSyncScheduler(impl: FirestoreUserSettingsRealtimeSyncManager): UserSettingsRealtimeSyncScheduler
}