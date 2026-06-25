package kr.co.domain.service.remoteconfig.repository

interface RemoteConfigRepository {
    suspend fun fetchAndActivate(): Boolean
    fun getLastModifiedAt(): Long
    fun isMaintenanceMode(): Boolean
    fun getMaintenanceReason(): String
    fun isUserDataSyncEnabled(): Boolean
    fun isProfilePhotoUploadEnabled(): Boolean
    fun isDiarySyncEnabled(): Boolean
    fun isAiEnabled(): Boolean
}