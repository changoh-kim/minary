package kr.co.data.feature.user.source.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.co.data.local.config.LocalStoragePathProvider
import kr.co.data.local.provider.UserDatabaseProvider
import kr.co.data.local.provider.UserInternalStorageProvider
import kr.co.data.local.provider.UserProfileDataStoreProvider
import kr.co.data.local.provider.UserSettingsDataStoreProvider
import kr.co.data.local.provider.UserSyncDataStoreProvider
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStorageLocalDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userDatabaseProvider: UserDatabaseProvider,
    private val userProfileDataStoreProvider: UserProfileDataStoreProvider,
    private val userSettingsDataStoreProvider: UserSettingsDataStoreProvider,
    private val userSyncDataStoreProvider: UserSyncDataStoreProvider,
    private val userInternalStorageProvider: UserInternalStorageProvider,
    private val pathProvider: LocalStoragePathProvider,
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
     * 특정 사용자의 내부 저장소에 프로필 이미지 파일을 저장합니다.
     */
    fun saveUserProfilePhoto(uid: String, photoUri: Uri): Uri? {
        val destinationFile = File(userDirectory, pathProvider.getUserProfilePhotoFileName(uid))
        return try {
            context.contentResolver.openInputStream(photoUri)?.use { input ->
                FileOutputStream(destinationFile).use { output -> input.copyTo(output) }
            }
            Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 특정 사용자의 내부 저장소에 프로필 이미지 파일을 다운로드하여 저장합니다.
     */
    fun downloadUserProfilePhoto(uid: String, downloadUrl: String): Uri? {
        val destinationFile = File(userDirectory, pathProvider.getUserProfilePhotoFileName(uid))
        return try {
            java.net.URL(downloadUrl).openStream().use { input ->
                FileOutputStream(destinationFile).use { output -> input.copyTo(output) }
            }
            Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
