package kr.co.minary.testing

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kr.co.data.di.feature.AccountModule
import kr.co.data.di.feature.CalendarModule
import kr.co.data.di.feature.DashboardModule
import kr.co.data.di.feature.DiaryModule
import kr.co.data.di.feature.SearchModule
import kr.co.data.di.feature.SessionModule
import kr.co.data.di.feature.TimeModule
import kr.co.data.di.feature.UserModule
import kr.co.data.di.feature.UserProfileModule
import kr.co.data.di.feature.UserSettingsModule
import kr.co.data.di.service.ImageProcessorModule
import kr.co.data.di.service.NetworkModule
import kr.co.data.di.service.RemoteConfigModule
import kr.co.domain.feature.account.service.AccountService
import kr.co.domain.feature.calendar.repository.CalendarRepository
import kr.co.domain.feature.dashboard.repository.DashboardRepository
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.diary.sync.DiaryRealtimeSyncManager
import kr.co.domain.feature.diary.sync.DiarySyncManager
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import kr.co.domain.feature.diary.sync.DiarySyncStateRepository
import kr.co.domain.feature.profile.repository.UserProfileRepository
import kr.co.domain.feature.profile.sync.UserProfileRealtimeSyncScheduler
import kr.co.domain.feature.profile.sync.UserProfileSyncManager
import kr.co.domain.feature.profile.sync.UserProfileSyncScheduler
import kr.co.domain.feature.search.repository.SearchRepository
import kr.co.domain.feature.session.repository.SessionRepository
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.feature.setting.sync.UserSettingsRealtimeSyncScheduler
import kr.co.domain.feature.setting.sync.UserSettingsSyncManager
import kr.co.domain.feature.setting.sync.UserSettingsSyncScheduler
import kr.co.domain.feature.user.repository.UserDataSyncStateRepository
import kr.co.domain.feature.user.repository.UserStorageRepository
import kr.co.domain.service.image.ImageProcessor
import kr.co.domain.service.network.NetworkMonitor
import kr.co.domain.service.remoteconfig.repository.RemoteConfigRepository
import kr.co.domain.service.time.ServerTimeProvider
import kr.co.domain.service.time.ServerTimeSyncScheduler
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        AccountModule::class,
        CalendarModule::class,
        DashboardModule::class,
        DiaryModule::class,
        SearchModule::class,
        SessionModule::class,
        TimeModule::class,
        UserModule::class,
        UserProfileModule::class,
        UserSettingsModule::class,
        ImageProcessorModule::class,
        NetworkModule::class,
        RemoteConfigModule::class,
    ],
)
interface FakeAppBackendModule {
    @Binds
    @Singleton
    fun bindAccountService(fake: FakeAppBackend): AccountService

    @Binds
    @Singleton
    fun bindCalendarRepository(fake: FakeAppBackend): CalendarRepository

    @Binds
    @Singleton
    fun bindDashboardRepository(fake: FakeAppBackend): DashboardRepository

    @Binds
    @Singleton
    fun bindDiaryRepository(fake: FakeAppBackend): DiaryRepository

    @Binds
    @Singleton
    fun bindDiarySyncStateRepository(fake: FakeAppBackend): DiarySyncStateRepository

    @Binds
    @Singleton
    fun bindDiarySyncScheduler(fake: FakeAppBackend): DiarySyncScheduler

    @Binds
    @Singleton
    fun bindDiarySyncManager(fake: FakeAppBackend): DiarySyncManager

    @Binds
    @Singleton
    fun bindDiaryRealtimeSyncManager(fake: FakeAppBackend): DiaryRealtimeSyncManager

    @Binds
    @Singleton
    fun bindUserProfileRepository(fake: FakeAppBackend): UserProfileRepository

    @Binds
    @Singleton
    fun bindUserProfileSyncScheduler(fake: FakeAppBackend): UserProfileSyncScheduler

    @Binds
    @Singleton
    fun bindUserProfileSyncManager(fake: FakeAppBackend): UserProfileSyncManager

    @Binds
    @Singleton
    fun bindUserProfileRealtimeSyncScheduler(fake: FakeAppBackend): UserProfileRealtimeSyncScheduler

    @Binds
    @Singleton
    fun bindSearchRepository(fake: FakeAppBackend): SearchRepository

    @Binds
    @Singleton
    fun bindSessionRepository(fake: FakeAppBackend): SessionRepository

    @Binds
    @Singleton
    fun bindUserSettingsRepository(fake: FakeAppBackend): UserSettingsRepository

    @Binds
    @Singleton
    fun bindUserSettingsSyncScheduler(fake: FakeAppBackend): UserSettingsSyncScheduler

    @Binds
    @Singleton
    fun bindUserSettingsSyncManager(fake: FakeAppBackend): UserSettingsSyncManager

    @Binds
    @Singleton
    fun bindUserSettingsRealtimeSyncScheduler(fake: FakeAppBackend): UserSettingsRealtimeSyncScheduler

    @Binds
    @Singleton
    fun bindUserDataSyncStateRepository(fake: FakeAppBackend): UserDataSyncStateRepository

    @Binds
    @Singleton
    fun bindUserStorageRepository(fake: FakeAppBackend): UserStorageRepository

    @Binds
    @Singleton
    fun bindServerTimeProvider(fake: FakeAppBackend): ServerTimeProvider

    @Binds
    @Singleton
    fun bindServerTimeSyncScheduler(fake: FakeAppBackend): ServerTimeSyncScheduler

    @Binds
    @Singleton
    fun bindImageProcessor(fake: FakeAppBackend): ImageProcessor

    @Binds
    @Singleton
    fun bindNetworkMonitor(fake: FakeAppBackend): NetworkMonitor

    @Binds
    @Singleton
    fun bindRemoteConfigRepository(fake: FakeAppBackend): RemoteConfigRepository
}
