package kr.co.domain.feature.setting.service.sync


interface UserSettingsRealtimeSyncScheduler {
    suspend fun startListening()
    fun stopListening()
}