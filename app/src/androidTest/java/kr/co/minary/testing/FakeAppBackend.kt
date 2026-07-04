package kr.co.minary.testing

import androidx.paging.PagingData
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kr.co.core.common.error.DomainError
import kr.co.core.common.model.Emotion
import kr.co.core.common.model.AppTheme
import kr.co.core.common.model.Gender
import kr.co.core.common.result.AppResult
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.common.state.SyncProcessState
import kr.co.core.common.state.SyncStatus
import kr.co.domain.feature.account.model.Account
import kr.co.domain.feature.account.model.SignUpInfo
import kr.co.domain.feature.account.service.AccountService
import kr.co.domain.feature.calendar.model.CalendarDay
import kr.co.domain.feature.calendar.model.CalendarMonth
import kr.co.domain.feature.calendar.repository.CalendarRepository
import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.domain.feature.dashboard.repository.DashboardRepository
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.diary.sync.DiaryRealtimeSyncManager
import kr.co.domain.feature.diary.sync.DiarySyncManager
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import kr.co.domain.feature.diary.sync.DiarySyncStateRepository
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.profile.repository.UserProfileRepository
import kr.co.domain.feature.profile.sync.UserProfileRealtimeSyncScheduler
import kr.co.domain.feature.profile.sync.UserProfileSyncManager
import kr.co.domain.feature.profile.sync.UserProfileSyncScheduler
import kr.co.domain.feature.search.repository.SearchRepository
import kr.co.domain.feature.session.model.UserSession
import kr.co.domain.feature.session.repository.SessionRepository
import kr.co.domain.feature.setting.model.UserSettings
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
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAppBackend @Inject constructor() :
    AccountService,
    CalendarRepository,
    DashboardRepository,
    DiaryRepository,
    DiarySyncStateRepository,
    DiarySyncScheduler,
    DiarySyncManager,
    DiaryRealtimeSyncManager,
    UserProfileRepository,
    UserProfileSyncScheduler,
    UserProfileSyncManager,
    UserProfileRealtimeSyncScheduler,
    SearchRepository,
    SessionRepository,
    UserSettingsRepository,
    UserSettingsSyncScheduler,
    UserSettingsSyncManager,
    UserSettingsRealtimeSyncScheduler,
    UserDataSyncStateRepository,
    UserStorageRepository,
    ServerTimeProvider,
    ServerTimeSyncScheduler,
    ImageProcessor,
    NetworkMonitor,
    RemoteConfigRepository {

    override val initDiarySyncState = MutableStateFlow<SyncProcessState>(SyncProcessState.Completed)
    override val pendingCount = MutableStateFlow(0)
    override val userDataSyncState = MutableStateFlow<SyncProcessState>(SyncProcessState.Idle)
    override val isOnline = MutableStateFlow(true)
    private val diaryChangeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    override val diaryChangeEvent: Flow<Unit> = diaryChangeEvents.asSharedFlow()

    private val sessionState = MutableStateFlow<UserSession?>(null)
    private val appThemeState = MutableStateFlow(AppTheme.SYSTEM)
    private val diarySyncEnabledState = MutableStateFlow(false)
    private val userSettingsState = MutableStateFlow<AppResult<UserSettings>>(Ok(defaultSettings()))
    private val userProfileState = MutableStateFlow<AppResult<UserProfile>>(Ok(defaultProfile()))
    private val recentSearchesState = MutableStateFlow<List<String>>(emptyList())
    private val diariesState = MutableStateFlow<List<Diary>>(emptyList())
    private val monthSyncStatusState = MutableStateFlow(SyncStatus.IDLE)

    private var lastSignInUid: String? = null
    private var lastUserDataSyncTimestamp = 0L
    private var initialSyncCompleted = true
    private var currentTime = DEFAULT_NOW
    private var maintenanceMode = false
    private var maintenanceReason = DEFAULT_MAINTENANCE_REASON
    private var signInResult: AppResult<Account> = Ok(defaultAccount())

    fun reset() {
        sessionState.value = null
        appThemeState.value = AppTheme.SYSTEM
        diarySyncEnabledState.value = false
        userSettingsState.value = Ok(defaultSettings())
        userProfileState.value = Ok(defaultProfile())
        recentSearchesState.value = emptyList()
        diariesState.value = emptyList()
        monthSyncStatusState.value = SyncStatus.IDLE
        initDiarySyncState.value = SyncProcessState.Completed
        pendingCount.value = 0
        userDataSyncState.value = SyncProcessState.Idle
        isOnline.value = true
        lastSignInUid = null
        lastUserDataSyncTimestamp = 0L
        initialSyncCompleted = true
        currentTime = DEFAULT_NOW
        maintenanceMode = false
        maintenanceReason = DEFAULT_MAINTENANCE_REASON
        signInResult = Ok(defaultAccount())
    }

    fun signInAsTestUser() {
        val session = defaultSession()
        sessionState.value = session
        lastSignInUid = session.uid
        userProfileState.value = Ok(defaultProfile(uid = session.uid, email = session.email))
    }

    fun setMaintenance(reason: String = DEFAULT_MAINTENANCE_REASON) {
        maintenanceMode = true
        maintenanceReason = reason
    }

    fun setSignInFailure(error: DomainError = DomainError.Auth.InvalidCredentials) {
        signInResult = Err(error)
    }

    fun seedDiary(
        date: LocalDate = LocalDate.of(2026, 1, 15),
        title: String = "title-test",
        content: String = "content-test",
    ): Diary {
        val diary = Diary(
            id = "diary-${date}",
            date = date,
            title = title,
            content = content,
            emotions = listOf(Emotion.JOY),
            createdAt = DEFAULT_NOW,
            updatedAt = DEFAULT_NOW,
            syncStatus = DiarySyncStatus.SYNCED,
        )
        upsertDiary(diary)
        return diary
    }

    fun diaryByDate(date: LocalDate): Diary? =
        diariesState.value.firstOrNull { it.date == date }

    fun diaryByTitle(title: String): Diary? =
        diariesState.value.firstOrNull { it.title == title }

    override suspend fun createAccount(signUpInfo: SignUpInfo, joinedAt: Long): AppResult<Unit> = Ok(Unit)

    override suspend fun deleteAccount(password: String): AppResult<String> {
        val uid = sessionState.value?.uid ?: TEST_UID
        sessionState.value = null
        return Ok(uid)
    }

    override suspend fun signIn(email: String, password: String): AppResult<Account> {
        val account = signInResult.get() ?: return signInResult
        val signedInAccount = account.copy(email = email)
        sessionState.value = UserSession(signedInAccount.uid, signedInAccount.email)
        return Ok(signedInAccount)
    }

    override suspend fun signOut(): AppResult<Unit> {
        sessionState.value = null
        return Ok(Unit)
    }

    override suspend fun checkEmailAvailability(email: String): AppResult<Boolean> = Ok(true)

    override fun getYearlyPages(targetYear: Year): Flow<PagingData<CalendarMonth>> =
        MutableStateFlow(PagingData.from((1..12).map { month ->
            calendarMonth(YearMonth.of(targetYear.value, month))
        }))

    override fun getMonthlyPages(targetYearMonth: YearMonth): Flow<PagingData<CalendarMonth>> =
        MutableStateFlow(PagingData.from(listOf(calendarMonth(targetYearMonth))))

    override suspend fun getDashboard(): AppResult<Dashboard> = Ok(Dashboard())

    override fun getSyncStatusStream(yearMonth: YearMonth): Flow<SyncStatus> = monthSyncStatusState

    override suspend fun requestMonthSync(userId: String, yearMonth: YearMonth): AppResult<Unit> = Ok(Unit)

    override suspend fun createDiary(diary: Diary): AppResult<Unit> {
        upsertDiary(diary)
        return Ok(Unit)
    }

    override suspend fun updateDiary(diary: Diary): AppResult<Diary> {
        upsertDiary(diary)
        return Ok(diary)
    }

    override suspend fun deleteDiary(diary: Diary): AppResult<Unit> {
        diariesState.value = diariesState.value.filterNot { it.date == diary.date }
        diaryChangeEvents.tryEmit(Unit)
        return Ok(Unit)
    }

    override suspend fun deleteOldDiaries(): AppResult<Unit> = Ok(Unit)

    override suspend fun getDiary(date: LocalDate): AppResult<Diary?> =
        Ok(diariesState.value.firstOrNull { it.date == date })

    override fun getDiaryStream(date: LocalDate): Flow<Diary?> =
        diariesState.map { diaries -> diaries.firstOrNull { it.date == date } }

    override fun getDiariesByDateRangeStream(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>> =
        diariesState.map { diaries ->
            diaries.filter { diary -> !diary.date.isBefore(startDate) && !diary.date.isAfter(endDate) }
        }

    override suspend fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): AppResult<List<Diary>> =
        Ok(diariesState.value.filter { diary -> !diary.date.isBefore(startDate) && !diary.date.isAfter(endDate) })

    override suspend fun getPagedDiaries(
        query: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Int,
        offset: Int,
    ): AppResult<List<Diary>> {
        val filtered = diariesState.value.filter { diary ->
            val matchesQuery = query.isNullOrBlank() ||
                diary.title.contains(query, ignoreCase = true) ||
                diary.content.contains(query, ignoreCase = true)
            val matchesStart = startDate == null || !diary.date.isBefore(startDate)
            val matchesEnd = endDate == null || !diary.date.isAfter(endDate)
            matchesQuery && matchesStart && matchesEnd
        }
        return Ok(filtered.drop(offset).take(limit))
    }

    override fun updateInitDiarySyncState(state: SyncProcessState) {
        initDiarySyncState.value = state
    }

    override suspend fun isInitialSyncCompleted(): Boolean = initialSyncCompleted

    override suspend fun setInitialSyncCompleted(completed: Boolean) {
        initialSyncCompleted = completed
    }

    override fun scheduleFullSync() = Unit
    override fun rescheduleFullSync() = Unit
    override fun scheduleImmediateSync() = Unit
    override fun schedulePeriodicSync() = Unit
    override fun cancelFullSync() = Unit
    override fun cancelImmediateSync() = Unit
    override fun cancelPeriodicSync() = Unit
    override fun cancelAllSync() = Unit

    override suspend fun performChunkedSync(userId: String): AppResult<Boolean> = Ok(true)
    override suspend fun performImmediatePush(userId: String): AppResult<Unit> = Ok(Unit)
    override suspend fun performMonthSync(userId: String, yearMonth: YearMonth): AppResult<Unit> = Ok(Unit)

    override suspend fun performInitialPull(userId: String, onProgress: (Float) -> Unit): AppResult<Unit> {
        onProgress(1f)
        return Ok(Unit)
    }

    override suspend fun startListening() = Unit
    override fun stopListening() = Unit

    override suspend fun getUserProfileStream(): Flow<AppResult<UserProfile>> = userProfileState

    override suspend fun updateUserProfile(profile: UserProfile): AppResult<Unit> {
        userProfileState.value = Ok(profile)
        return Ok(Unit)
    }

    override suspend fun updateUserProfilePhoto(uid: String, photoUrl: String, lastModifiedAt: Long): AppResult<String> {
        val current = userProfileState.value.get() ?: defaultProfile(uid = uid)
        userProfileState.value = Ok(current.copy(profilePhotoUrl = photoUrl, lastModifiedAt = lastModifiedAt))
        return Ok(photoUrl)
    }

    override fun scheduleProfilePush() = Unit
    override fun scheduleProfilePhotoPush() = Unit
    override fun cancelProfilePush() = Unit
    override fun cancelProfilePhotoPush() = Unit
    override fun cancelAll() = Unit

    override suspend fun syncProfile(userId: String): AppResult<Unit> = Ok(Unit)
    override suspend fun pushProfile(userId: String): AppResult<Unit> = Ok(Unit)
    override suspend fun pullProfile(userId: String): AppResult<Unit> = Ok(Unit)
    override suspend fun pushProfilePhoto(userId: String): AppResult<Unit> = Ok(Unit)

    override fun getRecentSearches(): Flow<List<String>> = recentSearchesState

    override suspend fun addRecentSearch(query: String, timestamp: Long) {
        recentSearchesState.value = listOf(query) + recentSearchesState.value.filterNot { it == query }
    }

    override suspend fun removeRecentSearch(query: String) {
        recentSearchesState.value = recentSearchesState.value.filterNot { it == query }
    }

    override suspend fun clearAll() {
        recentSearchesState.value = emptyList()
    }

    override suspend fun getLastSignInUid(): String? = lastSignInUid

    override suspend fun setLastSignInUid(uid: String) {
        lastSignInUid = uid
    }

    override suspend fun getCurrentUser(): AppResult<UserSession> =
        sessionState.value?.let { Ok(it) } ?: Err(DomainError.Auth.UserNotFound)

    override suspend fun reload(): AppResult<UserSession> = getCurrentUser()

    override fun getSessionStateStream(): Flow<UserSession?> = sessionState

    override suspend fun getUserSettingsStream(): Flow<AppResult<UserSettings>> = userSettingsState

    override fun getAppThemeStream(): Flow<AppTheme> = appThemeState

    override suspend fun updateAppTheme(uid: String, appTheme: AppTheme, lastModifiedAt: Long): AppResult<Unit> {
        appThemeState.value = appTheme
        userSettingsState.value = Ok(currentSettings().copy(appTheme = appTheme, lastModifiedAt = lastModifiedAt))
        return Ok(Unit)
    }

    override fun getDiarySyncEnabledStream(): Flow<Boolean> = diarySyncEnabledState

    override suspend fun updateDiarySyncEnabled(uid: String, enabled: Boolean, lastModifiedAt: Long): AppResult<Unit> {
        diarySyncEnabledState.value = enabled
        userSettingsState.value = Ok(currentSettings().copy(diarySyncEnabled = enabled, lastModifiedAt = lastModifiedAt))
        return Ok(Unit)
    }

    override fun scheduleSettingsPush() = Unit
    override fun cancelSettingsPush() = Unit

    override suspend fun syncSettings(userId: String): AppResult<Unit> = Ok(Unit)
    override suspend fun pushSettings(userId: String): AppResult<Unit> = Ok(Unit)
    override suspend fun pullSettings(userId: String): AppResult<Unit> = Ok(Unit)

    override fun updateUserDataSyncState(state: SyncProcessState) {
        userDataSyncState.value = state
    }

    override suspend fun getLastSyncTimestamp(): Long = lastUserDataSyncTimestamp

    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        lastUserDataSyncTimestamp = timestamp
    }

    override suspend fun deleteUserStorage(uid: String): AppResult<Unit> = Ok(Unit)

    override suspend fun sync(): AppResult<Unit> = Ok(Unit)

    override fun now(): Long = currentTime

    override fun scheduleSync() = Unit
    override fun cancelSync() = Unit

    override suspend fun resizeImage(sourceUrl: String, targetUrl: String, maxWidth: Int, maxHeight: Int): AppResult<String> =
        Ok(targetUrl)

    override suspend fun fetchAndActivate(): Boolean = true
    override fun getLastModifiedAt(): Long = currentTime
    override fun isMaintenanceMode(): Boolean = maintenanceMode
    override fun getMaintenanceReason(): String = maintenanceReason
    override fun isUserDataSyncEnabled(): Boolean = true
    override fun isProfilePhotoUploadEnabled(): Boolean = true
    override fun isDiarySyncEnabled(): Boolean = true
    override fun isAiEnabled(): Boolean = true

    private fun upsertDiary(diary: Diary) {
        diariesState.value = diariesState.value.filterNot { it.date == diary.date } + diary
        diaryChangeEvents.tryEmit(Unit)
    }

    private fun currentSettings(): UserSettings =
        userSettingsState.value.get() ?: defaultSettings()

    private fun calendarMonth(yearMonth: YearMonth): CalendarMonth {
        val first = yearMonth.atDay(1)
        val start = first.minusDays(first.dayOfWeek.value % 7L)
        val days = (0 until 42).map { index ->
            val date = start.plusDays(index.toLong())
            CalendarDay(
                date = date,
                isCurrentMonth = date.month == yearMonth.month,
                diary = diariesState.value.firstOrNull { it.date == date },
            )
        }
        return CalendarMonth(yearMonth = yearMonth, days = days, syncStatus = SyncStatus.IDLE)
    }

    private companion object {
        const val TEST_UID = "uid-test"
        const val TEST_EMAIL = "user@example.com"
        const val DEFAULT_NOW = 1_700_000_000_000L
        const val DEFAULT_MAINTENANCE_REASON = "maintenance-test"

        fun defaultAccount() = Account(uid = TEST_UID, email = TEST_EMAIL)
        fun defaultSession() = UserSession(uid = TEST_UID, email = TEST_EMAIL)
        fun defaultSettings() = UserSettings(appTheme = AppTheme.SYSTEM, diarySyncEnabled = false, lastModifiedAt = DEFAULT_NOW)
        fun defaultProfile(uid: String = TEST_UID, email: String = TEST_EMAIL) = UserProfile(
            uid = uid,
            email = email,
            name = "User Test",
            gender = Gender.NONE,
            birthday = LocalDate.of(2000, 1, 1),
            nickname = "tester",
            joinedAt = DEFAULT_NOW,
            lastModifiedAt = DEFAULT_NOW,
        )
    }
}
