package kr.co.data.testing.fake

import android.net.Uri
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kr.co.core.common.model.Emotion
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.database.entity.DiarySyncMetadataEntity
import kr.co.core.database.entity.RecentSearchEntity
import kr.co.core.database.model.DiaryWithRelations
import kr.co.core.datastore.proto.UserProfileProto
import kr.co.core.datastore.proto.UserSettingsProto
import kr.co.core.datastore.proto.copy
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiary
import kr.co.data.feature.diary.source.local.DiaryLocalDataSource
import kr.co.data.feature.emotion.source.remote.EmotionRemoteDataSource
import kr.co.data.feature.profile.source.local.UserProfileLocalDataSource
import kr.co.data.feature.search.source.SearchLocalDataSource
import kr.co.data.feature.session.model.UserSessionModel
import kr.co.data.feature.session.source.local.SessionLocalDataSource
import kr.co.data.feature.session.source.remote.SessionRemoteDataSource
import kr.co.data.feature.setting.source.local.UserSettingsLocalDataSource
import kr.co.data.feature.user.source.local.UserStorageLocalDataSource
import kr.co.domain.feature.diary.model.Diary
import java.time.LocalDate
import java.time.YearMonth

class FakeSearchLocalDataSource {
    val mock: SearchLocalDataSource = mockk()
    val recentSearches = MutableStateFlow<List<String>>(emptyList())
    val addedSearches = mutableListOf<RecentSearchEntity>()
    val removedQueries = mutableListOf<String>()
    var clearAllCallCount = 0
    var failure: Throwable? = null

    init {
        every { mock.getRecentSearches() } returns recentSearches
        coEvery { mock.addRecentSearch(any()) } coAnswers {
            failure?.let { throw it }
            addedSearches += firstArg<RecentSearchEntity>()
        }
        coEvery { mock.removeRecentSearch(any()) } coAnswers {
            failure?.let { throw it }
            removedQueries += firstArg<String>()
        }
        coEvery { mock.clearAll() } coAnswers {
            failure?.let { throw it }
            clearAllCallCount++
            recentSearches.value = emptyList()
        }
    }
}

class FakeSessionLocalDataSource {
    val mock: SessionLocalDataSource = mockk()
    val setLastSignInUidCalls = mutableListOf<String>()
    var lastSignInUid: String? = null
    var failure: Throwable? = null

    init {
        coEvery { mock.getLastSignInUid() } coAnswers {
            failure?.let { throw it }
            lastSignInUid
        }
        coEvery { mock.setLastSignInUid(any()) } coAnswers {
            failure?.let { throw it }
            firstArg<String>().also {
                setLastSignInUidCalls += it
                lastSignInUid = it
            }
        }
    }
}

class FakeSessionRemoteDataSource {
    val mock: SessionRemoteDataSource = mockk()
    val sessionState = MutableStateFlow<UserSessionModel?>(null)
    var currentUser: UserSessionModel? = null
    var reloadUser: UserSessionModel? = null
    var currentUserFailure: Throwable? = null
    var reloadFailure: Throwable? = null

    init {
        every { mock.getCurrentUser() } answers {
            currentUserFailure?.let { throw it }
            requireNotNull(currentUser) { "Current user is not configured." }
        }
        coEvery { mock.reload() } coAnswers {
            reloadFailure?.let { throw it }
            requireNotNull(reloadUser ?: currentUser) { "Reload user is not configured." }
        }
        every { mock.observeSessionStateFlow() } returns sessionState
    }
}

class FakeEmotionRemoteDataSource {
    val mock: EmotionRemoteDataSource = mockk()
    val analyzedDiaries = mutableListOf<Diary>()
    var emotions: List<Emotion> = listOf(Emotion.UNKNOWN)
    var failure: Throwable? = null

    init {
        coEvery { mock.analysis(any()) } coAnswers {
            failure?.let { throw it }
            firstArg<Diary>().also { analyzedDiaries += it }
            emotions
        }
    }
}

class FakeDiaryLocalDataSource {
    val mock: DiaryLocalDataSource = mockk()
    val relationsState = MutableStateFlow<Map<String, DiaryWithRelations>>(emptyMap())
    val inserted = mutableListOf<DiaryWithRelations>()
    val updated = mutableListOf<DiaryWithRelations>()
    val upserted = mutableListOf<DiaryWithRelations>()
    val deletedIds = mutableListOf<String>()
    val deletedOldCutoffs = mutableListOf<Long>()
    val markedAsSyncedIds = mutableListOf<String>()
    val syncMetadataUpdates = mutableListOf<DiarySyncMetadataEntity>()
    var pendingRelations: List<DiaryWithRelations> = emptyList()
    var lastModifiedAt: Long = 0L
    var failure: Throwable? = null

    private val metadataStateByMonth =
        mutableMapOf<YearMonth, MutableStateFlow<DiarySyncMetadataEntity?>>()

    init {
        every { mock.getSyncMetadataStream(any()) } answers {
            val yearMonth = firstArg<YearMonth>()
            metadataStateByMonth.getOrPut(yearMonth) { MutableStateFlow(null) }
        }
        coEvery { mock.updateSyncMetadata(any()) } coAnswers {
            failure?.let { throw it }
            val metadata = firstArg<DiarySyncMetadataEntity>()
            syncMetadataUpdates += metadata
            metadataStateByMonth.getOrPut(metadata.yearMonth) { MutableStateFlow(null) }.value = metadata
        }
        coEvery { mock.getDiary(any()) } coAnswers {
            failure?.let { throw it }
            relationsState.value[firstArg<String>()]?.diary
        }
        coEvery { mock.insert(any()) } coAnswers {
            failure?.let { throw it }
            val relations = firstArg<DiaryWithRelations>()
            inserted += relations
            put(relations)
        }
        coEvery { mock.update(any()) } coAnswers {
            failure?.let { throw it }
            val relations = firstArg<DiaryWithRelations>()
            updated += relations
            put(relations)
        }
        coEvery { mock.upsert(any()) } coAnswers {
            failure?.let { throw it }
            val relations = firstArg<DiaryWithRelations>()
            upserted += relations
            put(relations)
        }
        coEvery { mock.delete(any()) } coAnswers {
            failure?.let { throw it }
            val diaryId = firstArg<String>()
            deletedIds += diaryId
            relationsState.value = relationsState.value - diaryId
        }
        coEvery { mock.deleteOldDiaries(any()) } coAnswers {
            failure?.let { throw it }
            deletedOldCutoffs += firstArg<Long>()
        }
        coEvery { mock.getSyncStatus(any()) } coAnswers {
            failure?.let { throw it }
            relationsState.value[firstArg<String>()]?.diary?.syncStatus
        }
        coEvery { mock.getDiaryWithRelations(any<LocalDate>()) } coAnswers {
            failure?.let { throw it }
            val date = firstArg<LocalDate>()
            relationsState.value.values.firstOrNull { it.diary.date == date }
        }
        coEvery { mock.getDiaryWithRelations(any<String>()) } coAnswers {
            failure?.let { throw it }
            relationsState.value[firstArg<String>()]
        }
        every { mock.getDiaryStream(any()) } answers {
            val date = firstArg<LocalDate>()
            relationsState.map { map ->
                map.values.firstOrNull { it.diary.date == date }?.toDiary()
            }
        }
        every { mock.getDiariesByDateRangeStream(any(), any()) } answers {
            val startDate = firstArg<LocalDate>()
            val endDate = secondArg<LocalDate>()
            relationsState.map { map ->
                map.values
                    .filter { !it.diary.date.isBefore(startDate) && !it.diary.date.isAfter(endDate) }
                    .sortedBy { it.diary.date }
                    .map { it.toDiary() }
            }
        }
        coEvery { mock.getDiariesByDateRange(any(), any()) } coAnswers {
            failure?.let { throw it }
            val startDate = firstArg<LocalDate>()
            val endDate = secondArg<LocalDate>()
            relationsState.value.values
                .filter { !it.diary.date.isBefore(startDate) && !it.diary.date.isAfter(endDate) }
                .sortedBy { it.diary.date }
                .map { it.toDiary() }
        }
        coEvery { mock.getPagedDiaries(any(), any(), any(), any(), any()) } coAnswers {
            failure?.let { throw it }
            relationsState.value.values.map { it.toDiary() }
        }
        coEvery { mock.getPendingItemCount() } coAnswers {
            failure?.let { throw it }
            pendingRelations.ifEmpty {
                relationsState.value.values.filter { it.diary.syncStatus != DiarySyncStatus.SYNCED }
            }.size
        }
        coEvery { mock.getPendingDiariesWithRelations(any()) } coAnswers {
            failure?.let { throw it }
            pendingRelations.take(firstArg())
        }
        coEvery { mock.markAsSynced(any()) } coAnswers {
            failure?.let { throw it }
            val diaryId = firstArg<String>()
            markedAsSyncedIds += diaryId
            relationsState.value[diaryId]?.let {
                put(it.copy(diary = it.diary.copy(syncStatus = DiarySyncStatus.SYNCED)))
            }
        }
        coEvery { mock.getLastModifiedAt() } coAnswers {
            failure?.let { throw it }
            lastModifiedAt
        }
    }

    fun seed(vararg relations: DiaryWithRelations) {
        relationsState.value = relations.associateBy { it.diary.id }
    }

    private fun put(relations: DiaryWithRelations) {
        relationsState.value = relationsState.value + (relations.diary.id to relations)
    }
}

class FakeUserProfileLocalDataSource {
    val mock: UserProfileLocalDataSource = mockk()
    val profileState = MutableStateFlow(UserProfileProto.getDefaultInstance())
    val updatedProfiles = mutableListOf<UserProfileProto>()
    val updatedPhotoUrls = mutableListOf<Pair<String, Long>>()
    val requestedPhotoPaths = mutableListOf<String>()
    val downloadRequests = mutableListOf<DownloadProfilePhotoRequest>()
    var profilePhotoFilePath: String = "/tmp/user-profile-photo-test.jpg"
    var downloadedProfilePhotoUri: Uri? = null
    var failure: Throwable? = null

    init {
        every { mock.getUserProfileFlow() } returns profileState
        coEvery { mock.getUserProfile() } coAnswers {
            failure?.let { throw it }
            profileState.value
        }
        coEvery { mock.updateUserProfile(any()) } coAnswers {
            failure?.let { throw it }
            val profile = firstArg<UserProfileProto>()
            updatedProfiles += profile
            profileState.value = profile
        }
        coEvery { mock.updateUserProfilePhotoUrl(any(), any()) } coAnswers {
            failure?.let { throw it }
            val profilePhotoUrl = firstArg<String>()
            val lastModifiedAt = secondArg<Long>()
            updatedPhotoUrls += profilePhotoUrl to lastModifiedAt
            profileState.value = profileState.value.copy {
                this.profilePhotoUrl = profilePhotoUrl
                this.lastModifiedAt = lastModifiedAt
            }
        }
        every { mock.getProfilePhotoFilePath(any()) } answers {
            firstArg<String>().also { requestedPhotoPaths += it }
            profilePhotoFilePath
        }
        coEvery { mock.downloadAndSyncProfilePhoto(any(), any(), any()) } coAnswers {
            failure?.let { throw it }
            val request = DownloadProfilePhotoRequest(
                uid = firstArg(),
                downloadUrl = secondArg(),
                lastModifiedAt = thirdArg(),
            )
            downloadRequests += request
            val localUrl = downloadedProfilePhotoUri?.toString() ?: request.downloadUrl
            profileState.value = profileState.value.copy {
                profilePhotoUrl = localUrl
                lastModifiedAt = request.lastModifiedAt
            }
        }
    }
}

data class DownloadProfilePhotoRequest(
    val uid: String,
    val downloadUrl: String,
    val lastModifiedAt: Long,
)

class FakeUserSettingsLocalDataSource {
    val mock: UserSettingsLocalDataSource = mockk()
    val settingsState = MutableStateFlow(UserSettingsProto.getDefaultInstance())
    val updatedSettings = mutableListOf<UserSettingsProto>()
    var failure: Throwable? = null

    init {
        every { mock.getUserSettingsFlow() } returns settingsState
        coEvery { mock.getUserSettings() } coAnswers {
            failure?.let { throw it }
            settingsState.value
        }
        every { mock.getAppThemeFlow() } returns settingsState.map { it.theme }
        every { mock.getDiarySyncEnabledFlow() } returns settingsState.map { it.diarySyncEnabled }
        coEvery { mock.updateUserSettings(any()) } coAnswers {
            failure?.let { throw it }
            val settings = firstArg<UserSettingsProto>()
            updatedSettings += settings
            settingsState.value = settings
        }
    }
}

class FakeUserStorageLocalDataSource {
    val mock: UserStorageLocalDataSource = mockk()
    val deletedUids = mutableListOf<String>()
    val requestedProfilePhotoPaths = mutableListOf<String>()
    val requestedTemporaryPhotoPaths = mutableListOf<String>()
    val downloadRequests = mutableListOf<Pair<String, String>>()
    var profilePhotoFilePath = "/tmp/user-profile-photo-test.jpg"
    var temporaryProfilePhotoFilePath = "/tmp/user-temp-profile-photo-test.jpg"
    var downloadedUri: Uri? = null
    var failure: Throwable? = null

    init {
        every { mock.deleteUserStorage(any()) } answers {
            failure?.let { throw it }
            deletedUids += firstArg<String>()
        }
        every { mock.getUserProfilePhotoFilePath(any()) } answers {
            requestedProfilePhotoPaths += firstArg<String>()
            profilePhotoFilePath
        }
        every { mock.getTemporaryProfilePhotoFilePath(any()) } answers {
            requestedTemporaryPhotoPaths += firstArg<String>()
            temporaryProfilePhotoFilePath
        }
        coEvery { mock.downloadUserProfilePhoto(any(), any()) } coAnswers {
            failure?.let { throw it }
            downloadRequests += firstArg<String>() to secondArg<String>()
            downloadedUri
        }
    }
}
