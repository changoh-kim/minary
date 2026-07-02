package kr.co.data.di

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.mockk
import kr.co.data.di.service.ImageProcessorModule
import kr.co.data.di.service.NetworkModule
import kr.co.data.feature.account.service.FirebaseAccountService
import kr.co.data.feature.calendar.repository.CalendarRepositoryImpl
import kr.co.data.feature.dashboard.repository.DashboardRepositoryImpl
import kr.co.data.feature.diary.repository.DiaryRepositoryImpl
import kr.co.data.feature.diary.sync.DiarySyncStateRepositoryImpl
import kr.co.data.feature.diary.sync.DiarySyncWorkManager
import kr.co.data.feature.diary.sync.FirestoreDiaryRealtimeSyncManager
import kr.co.data.feature.diary.sync.FirestoreDiarySyncManager
import kr.co.data.feature.profile.repository.UserProfileRepositoryImpl
import kr.co.data.feature.profile.sync.FirestoreUserProfileRealtimeSyncManager
import kr.co.data.feature.profile.sync.FirestoreUserProfileSyncManager
import kr.co.data.feature.profile.sync.UserProfileSyncSchedulerWorkManager
import kr.co.data.feature.search.repository.SearchRepositoryImpl
import kr.co.data.feature.session.repository.SessionRepositoryImpl
import kr.co.data.feature.setting.repository.UserSettingsRepositoryImpl
import kr.co.data.feature.setting.sync.FirestoreUserSettingsRealtimeSyncManager
import kr.co.data.feature.setting.sync.FirestoreUserSettingsSyncManager
import kr.co.data.feature.setting.sync.UserSettingsSyncSchedulerWorkManager
import kr.co.data.feature.user.repository.UserDataSyncStateRepositoryImpl
import kr.co.data.feature.user.repository.UserStorageRepositoryImpl
import kr.co.data.service.remoteconfig.RemoteConfigRepositoryImpl
import kr.co.data.service.time.TrueTimeProvider
import kr.co.data.service.time.sync.ServerTimeSyncWorkManager
import kr.co.data.testing.BaseDataHiltInstrumentationTest
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
import kr.co.domain.service.remoteconfig.repository.RemoteConfigRepository
import kr.co.domain.service.time.ServerTimeProvider
import kr.co.domain.service.time.ServerTimeSyncScheduler
import org.junit.Assert.assertSame
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(
    ImageProcessorModule::class,
    NetworkModule::class,
    MinaryDataModule::class,
)
class DataFeatureModuleHiltSmokeTest : BaseDataHiltInstrumentationTest() {
    @BindValue
    @JvmField
    val accountServiceImpl: FirebaseAccountService = mockk(relaxed = true)

    @BindValue
    @JvmField
    val calendarRepositoryImpl: CalendarRepositoryImpl = mockk(relaxed = true)

    @BindValue
    @JvmField
    val dashboardRepositoryImpl: DashboardRepositoryImpl = mockk(relaxed = true)

    @BindValue
    @JvmField
    val diaryRepositoryImpl: DiaryRepositoryImpl = mockk(relaxed = true)

    @BindValue
    @JvmField
    val diarySyncStateRepositoryImpl: DiarySyncStateRepositoryImpl = mockk(relaxed = true)

    @BindValue
    @JvmField
    val diarySyncSchedulerImpl: DiarySyncWorkManager = mockk(relaxed = true)

    @BindValue
    @JvmField
    val diarySyncManagerImpl: FirestoreDiarySyncManager = mockk(relaxed = true)

    @BindValue
    @JvmField
    val diaryRealtimeSyncManagerImpl: FirestoreDiaryRealtimeSyncManager = mockk(relaxed = true)

    @BindValue
    @JvmField
    val sessionRepositoryImpl: SessionRepositoryImpl = mockk(relaxed = true)

    @BindValue
    @JvmField
    val searchRepositoryImpl: SearchRepositoryImpl = mockk(relaxed = true)

    @BindValue
    @JvmField
    val userStorageRepositoryImpl: UserStorageRepositoryImpl = mockk(relaxed = true)

    @BindValue
    @JvmField
    val userDataSyncStateRepositoryImpl: UserDataSyncStateRepositoryImpl = mockk(relaxed = true)

    @BindValue
    @JvmField
    val userProfileRepositoryImpl: UserProfileRepositoryImpl = mockk(relaxed = true)

    @BindValue
    @JvmField
    val userProfileSyncSchedulerImpl: UserProfileSyncSchedulerWorkManager = mockk(relaxed = true)

    @BindValue
    @JvmField
    val userProfileSyncManagerImpl: FirestoreUserProfileSyncManager = mockk(relaxed = true)

    @BindValue
    @JvmField
    val userProfileRealtimeSyncSchedulerImpl: FirestoreUserProfileRealtimeSyncManager = mockk(relaxed = true)

    @BindValue
    @JvmField
    val userSettingsRepositoryImpl: UserSettingsRepositoryImpl = mockk(relaxed = true)

    @BindValue
    @JvmField
    val userSettingsSyncSchedulerImpl: UserSettingsSyncSchedulerWorkManager = mockk(relaxed = true)

    @BindValue
    @JvmField
    val userSettingsSyncManagerImpl: FirestoreUserSettingsSyncManager = mockk(relaxed = true)

    @BindValue
    @JvmField
    val userSettingsRealtimeSyncSchedulerImpl: FirestoreUserSettingsRealtimeSyncManager = mockk(relaxed = true)

    @BindValue
    @JvmField
    val serverTimeProviderImpl: TrueTimeProvider = mockk(relaxed = true)

    @BindValue
    @JvmField
    val serverTimeSyncSchedulerImpl: ServerTimeSyncWorkManager = mockk(relaxed = true)

    @BindValue
    @JvmField
    val remoteConfigRepositoryImpl: RemoteConfigRepositoryImpl = mockk(relaxed = true)

    @Inject
    lateinit var accountService: AccountService

    @Inject
    lateinit var calendarRepository: CalendarRepository

    @Inject
    lateinit var dashboardRepository: DashboardRepository

    @Inject
    lateinit var diaryRepository: DiaryRepository

    @Inject
    lateinit var diarySyncStateRepository: DiarySyncStateRepository

    @Inject
    lateinit var diarySyncScheduler: DiarySyncScheduler

    @Inject
    lateinit var diarySyncManager: DiarySyncManager

    @Inject
    lateinit var diaryRealtimeSyncManager: DiaryRealtimeSyncManager

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var searchRepository: SearchRepository

    @Inject
    lateinit var userStorageRepository: UserStorageRepository

    @Inject
    lateinit var userDataSyncStateRepository: UserDataSyncStateRepository

    @Inject
    lateinit var userProfileRepository: UserProfileRepository

    @Inject
    lateinit var userProfileSyncScheduler: UserProfileSyncScheduler

    @Inject
    lateinit var userProfileSyncManager: UserProfileSyncManager

    @Inject
    lateinit var userProfileRealtimeSyncScheduler: UserProfileRealtimeSyncScheduler

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    @Inject
    lateinit var userSettingsSyncScheduler: UserSettingsSyncScheduler

    @Inject
    lateinit var userSettingsSyncManager: UserSettingsSyncManager

    @Inject
    lateinit var userSettingsRealtimeSyncScheduler: UserSettingsRealtimeSyncScheduler

    @Inject
    lateinit var serverTimeProvider: ServerTimeProvider

    @Inject
    lateinit var serverTimeSyncScheduler: ServerTimeSyncScheduler

    @Inject
    lateinit var remoteConfigRepository: RemoteConfigRepository

    @Test
    fun feature_modules_bind_domain_contracts_to_data_implementations() {
        assertSame(accountServiceImpl, accountService)
        assertSame(calendarRepositoryImpl, calendarRepository)
        assertSame(dashboardRepositoryImpl, dashboardRepository)
        assertSame(diaryRepositoryImpl, diaryRepository)
        assertSame(diarySyncStateRepositoryImpl, diarySyncStateRepository)
        assertSame(diarySyncSchedulerImpl, diarySyncScheduler)
        assertSame(diarySyncManagerImpl, diarySyncManager)
        assertSame(diaryRealtimeSyncManagerImpl, diaryRealtimeSyncManager)
        assertSame(sessionRepositoryImpl, sessionRepository)
        assertSame(searchRepositoryImpl, searchRepository)
        assertSame(userStorageRepositoryImpl, userStorageRepository)
        assertSame(userDataSyncStateRepositoryImpl, userDataSyncStateRepository)
        assertSame(userProfileRepositoryImpl, userProfileRepository)
        assertSame(userProfileSyncSchedulerImpl, userProfileSyncScheduler)
        assertSame(userProfileSyncManagerImpl, userProfileSyncManager)
        assertSame(userProfileRealtimeSyncSchedulerImpl, userProfileRealtimeSyncScheduler)
        assertSame(userSettingsRepositoryImpl, userSettingsRepository)
        assertSame(userSettingsSyncSchedulerImpl, userSettingsSyncScheduler)
        assertSame(userSettingsSyncManagerImpl, userSettingsSyncManager)
        assertSame(userSettingsRealtimeSyncSchedulerImpl, userSettingsRealtimeSyncScheduler)
        assertSame(serverTimeProviderImpl, serverTimeProvider)
        assertSame(serverTimeSyncSchedulerImpl, serverTimeSyncScheduler)
        assertSame(remoteConfigRepositoryImpl, remoteConfigRepository)
    }
}
