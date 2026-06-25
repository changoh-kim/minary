package kr.co.data.service.remoteconfig

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.core.common.extension.TAG
import kr.co.core.firebase.provider.FirebaseRemoteConfigProvider
import kr.co.data.BuildConfig
import kr.co.domain.service.remoteconfig.repository.RemoteConfigRepository
import javax.inject.Inject

class RemoteConfigRepositoryImpl @Inject constructor(
    private val firebaseRemoteConfigProvider: FirebaseRemoteConfigProvider
) : RemoteConfigRepository {

    companion object {
        private const val KEY_LAST_MODIFIED_AT = "last_modified_at"
        private const val KEY_IS_MAINTENANCE_MODE = "is_maintenance_mode"
        private const val KEY_MAINTENANCE_REASON = "maintenance_reason"
        private const val KEY_USER_DATA_SYNC_ENABLED = "user_data_sync_enabled"
        private const val KEY_PROFILE_PHOTO_UPLOAD_ENABLED = "profile_photo_upload_enabled"
        private const val KEY_DIARY_SYNC_ENABLED = "diary_sync_enabled"
        private const val KEY_AI_ENABLED = "ai_enabled"

        private val FETCH_INTERVAL = if (BuildConfig.DEBUG) 0L else 3600L
    }

    override suspend fun fetchAndActivate(): Boolean {
        var result = false

        try {
            withContext(Dispatchers.IO) {
                firebaseRemoteConfigProvider
                    .setMinimumFetchIntervalInSeconds(FETCH_INTERVAL)
                    .await()
                result = firebaseRemoteConfigProvider.fetchAndActivate().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetchAndActivate", e)
            result = false
        }

        return result
    }

    override fun isMaintenanceMode(): Boolean =
        firebaseRemoteConfigProvider.getBoolean(KEY_IS_MAINTENANCE_MODE)

    override fun getMaintenanceReason(): String =
        firebaseRemoteConfigProvider.getString(KEY_MAINTENANCE_REASON)

    override fun isUserDataSyncEnabled(): Boolean =
        firebaseRemoteConfigProvider.getBoolean(KEY_USER_DATA_SYNC_ENABLED)

    override fun isProfilePhotoUploadEnabled(): Boolean =
        firebaseRemoteConfigProvider.getBoolean(KEY_PROFILE_PHOTO_UPLOAD_ENABLED)

    override fun isDiarySyncEnabled(): Boolean =
        firebaseRemoteConfigProvider.getBoolean(KEY_DIARY_SYNC_ENABLED)

    override fun isAiEnabled(): Boolean =
        firebaseRemoteConfigProvider.getBoolean(KEY_AI_ENABLED)

    override fun getLastModifiedAt(): Long =
        firebaseRemoteConfigProvider.getLong(KEY_LAST_MODIFIED_AT)
}
