package kr.co.data.testing

import android.net.Uri
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import androidx.datastore.preferences.core.Preferences
import kr.co.core.common.logging.AppLogger
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.database.entity.DiarySyncMetadataEntity
import kr.co.core.database.model.DiaryWithRelations
import kr.co.core.datastore.proto.UserProfileProto
import kr.co.core.datastore.proto.UserSettingsProto
import kr.co.core.datastore.proto.copy
import kr.co.core.datastore.sync.UserSyncDataStoreProvider
import kr.co.core.datastore.sync.UserSyncPrefsDataStore
import kr.co.data.feature.diary.source.local.DiaryLocalDataSource
import kr.co.data.feature.profile.source.local.UserProfileLocalDataSource
import kr.co.data.feature.setting.source.local.UserSettingsLocalDataSource
import kr.co.domain.service.time.ServerTimeProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class AndroidFakeAppLogger : AppLogger {
    override fun v(message: String, vararg args: Any?) = Unit
    override fun d(message: String, vararg args: Any?) = Unit
    override fun i(message: String, vararg args: Any?) = Unit
    override fun w(message: String, vararg args: Any?) = Unit
    override fun w(throwable: Throwable, message: String, vararg args: Any?) = Unit
    override fun e(message: String, vararg args: Any?) = Unit
    override fun e(throwable: Throwable, message: String, vararg args: Any?) = Unit
}

class AndroidFakeDiaryLocalDataSource {
    val mock: DiaryLocalDataSource = mockk()
    val relationsState = MutableStateFlow<Map<String, DiaryWithRelations>>(emptyMap())
    val upserted = mutableListOf<DiaryWithRelations>()
    val deletedIds = mutableListOf<String>()
    val markedAsSyncedIds = mutableListOf<String>()
    val syncMetadataUpdates = mutableListOf<DiarySyncMetadataEntity>()
    var pendingRelations: List<DiaryWithRelations> = emptyList()
    var lastModifiedAt: Long = 0L

    init {
        every { mock.getSyncMetadataStream(any()) } returns MutableSharedFlow()
        coEvery { mock.updateSyncMetadata(any()) } coAnswers {
            syncMetadataUpdates += firstArg<DiarySyncMetadataEntity>()
        }
        coEvery { mock.getDiary(any()) } coAnswers {
            relationsState.value[firstArg<String>()]?.diary
        }
        coEvery { mock.upsert(any()) } coAnswers {
            val relations = firstArg<DiaryWithRelations>()
            upserted += relations
            put(relations)
        }
        coEvery { mock.delete(any()) } coAnswers {
            val diaryId = firstArg<String>()
            deletedIds += diaryId
            relationsState.value = relationsState.value - diaryId
        }
        coEvery { mock.getDiaryWithRelations(any<String>()) } coAnswers {
            relationsState.value[firstArg<String>()]
        }
        coEvery { mock.getPendingItemCount() } coAnswers {
            pendingRelations.ifEmpty {
                relationsState.value.values.filter { it.diary.syncStatus != DiarySyncStatus.SYNCED }
            }.size
        }
        coEvery { mock.getPendingDiariesWithRelations(any()) } coAnswers {
            pendingRelations.take(firstArg())
        }
        coEvery { mock.markAsSynced(any()) } coAnswers {
            val diaryId = firstArg<String>()
            markedAsSyncedIds += diaryId
            relationsState.value[diaryId]?.let {
                put(it.copy(diary = it.diary.copy(syncStatus = DiarySyncStatus.SYNCED)))
            }
        }
        coEvery { mock.getLastModifiedAt() } coAnswers { lastModifiedAt }
    }

    fun seed(vararg relations: DiaryWithRelations) {
        relationsState.value = relations.associateBy { it.diary.id }
    }

    private fun put(relations: DiaryWithRelations) {
        relationsState.value = relationsState.value + (relations.diary.id to relations)
    }
}

class AndroidFakeUserSyncDataStoreProvider(
    initialLastPullDiaryModifiedAt: Long = 0L,
) {
    val mock: UserSyncDataStoreProvider = mockk()
    val dataStore: UserSyncPrefsDataStore = mockk()
    val lastPullDiaryModifiedAtUpdates = mutableListOf<Long>()
    var lastPullDiaryModifiedAt: Long = initialLastPullDiaryModifiedAt

    init {
        every { mock.getDataStore() } returns dataStore
        coEvery { dataStore.getLastPullDiaryModifiedAt(any()) } coAnswers {
            lastPullDiaryModifiedAt
        }
        coEvery { dataStore.setLastPullDiaryModifiedAt(any()) } coAnswers {
            val updatedAt = firstArg<Long>()
            lastPullDiaryModifiedAtUpdates += updatedAt
            lastPullDiaryModifiedAt = updatedAt
            mockk<Preferences>()
        }
    }
}

class AndroidFakeServerTimeProvider(
    var now: Long = 0L,
) : ServerTimeProvider {
    override suspend fun sync() = com.github.michaelbull.result.Ok(Unit)
    override fun now(): Long = now
}

class AndroidFakeUserProfileLocalDataSource(
    initialProfile: UserProfileProto = UserProfileProto.getDefaultInstance(),
) {
    val mock: UserProfileLocalDataSource = mockk()
    val profileState = MutableStateFlow(initialProfile)
    val updatedProfiles = mutableListOf<UserProfileProto>()
    val updatedPhotoUrls = mutableListOf<Pair<String, Long>>()
    val requestedPhotoPaths = mutableListOf<String>()
    val downloadRequests = mutableListOf<AndroidDownloadProfilePhotoRequest>()
    var profilePhotoFilePath: String = "/tmp/user-profile-photo-test.jpg"
    var downloadedProfilePhotoUri: Uri? = null

    init {
        every { mock.getUserProfileFlow() } returns profileState
        coEvery { mock.getUserProfile() } coAnswers { profileState.value }
        coEvery { mock.updateUserProfile(any()) } coAnswers {
            val profile = firstArg<UserProfileProto>()
            updatedProfiles += profile
            profileState.value = profile
        }
        coEvery { mock.updateUserProfilePhotoUrl(any(), any()) } coAnswers {
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
            val request = AndroidDownloadProfilePhotoRequest(
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

data class AndroidDownloadProfilePhotoRequest(
    val uid: String,
    val downloadUrl: String,
    val lastModifiedAt: Long,
)

class AndroidFakeUserSettingsLocalDataSource(
    initialSettings: UserSettingsProto = UserSettingsProto.getDefaultInstance(),
) {
    val mock: UserSettingsLocalDataSource = mockk()
    val settingsState = MutableStateFlow(initialSettings)
    val updatedSettings = mutableListOf<UserSettingsProto>()

    init {
        every { mock.getUserSettingsFlow() } returns settingsState
        coEvery { mock.getUserSettings() } coAnswers { settingsState.value }
        every { mock.getAppThemeFlow() } returns settingsState.map { it.theme }
        every { mock.getDiarySyncEnabledFlow() } returns settingsState.map { it.diarySyncEnabled }
        coEvery { mock.updateUserSettings(any()) } coAnswers {
            val settings = firstArg<UserSettingsProto>()
            updatedSettings += settings
            settingsState.value = settings
        }
    }
}
