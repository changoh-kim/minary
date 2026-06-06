package kr.co.data.local.config

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStoragePathProvider @Inject constructor() {
    companion object {
        // App
        private const val NAME_MINARY_DATASTORE = "minary_datastore"

        // User Database
        private const val NAME_USER_DATABASE = "user_database"

        // User DataStore
        private const val NAME_USER_PROFILE = "user_profile"
        private const val NAME_USER_SETTINGS = "user_settings"
        private const val NAME_USER_SYNC = "user_sync"

        // User Directory
        private const val NAME_USER_DIR = "user_dir"
        private const val NAME_USER_PROFILE_PHOTO = "user_profile_photo"
        private const val NAME_TEMP_USER_PROFILE_PHOTO = "user_temp_profile_photo"
    }

    // App
    fun getMinaryDataStoreName() = NAME_MINARY_DATASTORE

    // User Database
    fun getUserDatabaseName(uid: String) = "${NAME_USER_DATABASE}_$uid"

    // User DataStore
    fun getUserProfileDataStoreName(uid: String) = "${NAME_USER_PROFILE}_$uid.pb"
    fun getUserSettingsDataStoreName(uid: String) = "${NAME_USER_SETTINGS}_$uid.pb"
    fun getUserSyncDataStoreName(uid: String) = "${NAME_USER_SYNC}_$uid.pb"

    // User Directory
    fun getUserDirectoryName(uid: String) = "${NAME_USER_DIR}_$uid"
    fun getUserProfilePhotoFileName(uid: String) = "${NAME_USER_PROFILE_PHOTO}_$uid.jpg"
    fun getUserTempProfilePhotoFileName(uid: String) = "${NAME_TEMP_USER_PROFILE_PHOTO}_$uid.jpg"
}