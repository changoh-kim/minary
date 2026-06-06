package kr.co.data.feature.profile.source.local

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kr.co.data.extension.TAG
import kr.co.data.feature.user.source.local.UserStorageLocalDataSource
import kr.co.data.local.provider.UserProfileDataStoreProvider
import kr.co.data.proto.UserProfileProto
import kr.co.data.proto.copy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileLocalDataSource @Inject constructor(
    private val userProfileDataStoreProvider: UserProfileDataStoreProvider,
    private val userStorageLocalDataSource: UserStorageLocalDataSource,
) {
    private val dataStore get() = userProfileDataStoreProvider.getDataStore()

    fun getUserProfileFlow(): Flow<UserProfileProto> {
        return dataStore.data.catch {
            Log.e(TAG, "Failed to read user profile from DataStore", it)
            emit(UserProfileProto.getDefaultInstance())
        }
    }

    suspend fun getUserProfile(): UserProfileProto {
        return dataStore.data.first()
    }

    suspend fun updateUserProfile(profile: UserProfileProto) {
        dataStore.updateData { profile }
    }

    suspend fun updateUserProfilePhotoUrl(profilePhotoUrl: String, lastModifiedAt: Long) {
        dataStore.updateData { currentProfile ->
            currentProfile.copy {
                this.profilePhotoUrl = profilePhotoUrl
                this.lastModifiedAt = lastModifiedAt
            }
        }
    }

    fun getProfilePhotoFilePath(uid: String): String {
        return userStorageLocalDataSource.getUserProfilePhotoFilePath(uid)
    }

    fun getTemporaryProfilePhotoFilePath(uid: String): String {
        return userStorageLocalDataSource.getTemporaryProfilePhotoFilePath(uid)
    }

    /**
     * 프로필 이미지를 다운로드하고 로컬 스토리지에 저장한 후 DataStore에 반영합니다.
     */
    suspend fun downloadAndSyncProfilePhoto(uid: String, downloadUrl: String, lastModifiedAt: Long) {
        val savedUri = withContext(Dispatchers.IO) {
            userStorageLocalDataSource.downloadUserProfilePhoto(uid, downloadUrl)
        }
        val localImageUrl = savedUri?.toString() ?: downloadUrl
        updateUserProfilePhotoUrl(localImageUrl, lastModifiedAt)
    }
}