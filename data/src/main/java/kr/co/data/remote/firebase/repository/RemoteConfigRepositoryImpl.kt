package kr.co.data.remote.firebase.repository

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kr.co.data.BuildConfig
import kr.co.data.extension.TAG
import kr.co.domain.infra.remote.repository.RemoteConfigRepository
import javax.inject.Inject

class RemoteConfigRepositoryImpl @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
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
                remoteConfig
                    .setConfigSettingsAsync(
                        remoteConfigSettings {
                            minimumFetchIntervalInSeconds = FETCH_INTERVAL
                        }
                    ).await()
                result = remoteConfig.fetchAndActivate().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetchAndActivate", e)
            result = false
        }

        return result
    }

    override fun isMaintenanceMode(): Boolean =
        remoteConfig.getBoolean(KEY_IS_MAINTENANCE_MODE)

    override fun getMaintenanceReason(): String =
        remoteConfig.getString(KEY_MAINTENANCE_REASON)

    override fun isUserDataSyncEnabled(): Boolean =
        remoteConfig.getBoolean(KEY_USER_DATA_SYNC_ENABLED)

    override fun isProfilePhotoUploadEnabled(): Boolean =
        remoteConfig.getBoolean(KEY_PROFILE_PHOTO_UPLOAD_ENABLED)

    override fun isDiarySyncEnabled(): Boolean =
        remoteConfig.getBoolean(KEY_DIARY_SYNC_ENABLED)

    override fun isAiEnabled(): Boolean =
        remoteConfig.getBoolean(KEY_AI_ENABLED)

    override fun getLastModifiedAt(): Long =
        remoteConfig.getLong(KEY_LAST_MODIFIED_AT)
}