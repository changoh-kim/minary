package kr.co.data.feature.user.source.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kr.co.core.database.provider.UserDatabaseProvider
import kr.co.core.datastore.profile.UserProfileDataStoreProvider
import kr.co.core.datastore.settings.UserSettingsDataStoreProvider
import kr.co.core.datastore.sync.UserSyncDataStoreProvider
import kr.co.core.firebase.provider.FirebaseStorageProvider
import kr.co.core.storage.config.LocalStoragePathProvider
import kr.co.core.storage.provider.UserInternalStorageProvider
import kr.co.core.common.logging.AppLogger
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStorageLocalDataSource @Inject constructor(
    private val logger: AppLogger,
    @param:ApplicationContext private val context: Context,
    private val userDatabaseProvider: UserDatabaseProvider,
    private val userProfileDataStoreProvider: UserProfileDataStoreProvider,
    private val userSettingsDataStoreProvider: UserSettingsDataStoreProvider,
    private val userSyncDataStoreProvider: UserSyncDataStoreProvider,
    private val userInternalStorageProvider: UserInternalStorageProvider,
    private val pathProvider: LocalStoragePathProvider,
    private val firebaseStorageProvider: FirebaseStorageProvider,
) {
    private val userDirectory get() = userInternalStorageProvider.getUserDirectory()

    fun deleteUserStorage(uid: String) {
        userDatabaseProvider.deleteDatabaseFile(uid)
        userProfileDataStoreProvider.deleteDataStoreFile(uid)
        userSettingsDataStoreProvider.deleteDataStoreFile(uid)
        userSyncDataStoreProvider.deleteDataStoreFile(uid)
        userInternalStorageProvider.deleteUserDirectory(uid)
    }

    fun getUserProfilePhotoFilePath(uid: String): String {
        return File(userDirectory, pathProvider.getUserProfilePhotoFileName(uid)).absolutePath
    }

    fun getTemporaryProfilePhotoFilePath(uid: String): String {
        return File(context.cacheDir, pathProvider.getUserTempProfilePhotoFileName(uid)).absolutePath
    }

    /**
     * 특정 사용자의 내부 저장소에 프로필 이미지 파일을 다운로드하여 저장합니다.
     */
    suspend fun downloadUserProfilePhoto(uid: String, downloadUrl: String): Uri? {
        val destinationFile = File(userDirectory, pathProvider.getUserProfilePhotoFileName(uid))
        return try {
            firebaseStorageProvider.getUserProfilePhotoRef(uid)
                .getFile(destinationFile)
                .await()

            Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            logger.e(e, "Failed to download user profile photo")
            null
        }
    }
}
