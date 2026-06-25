package kr.co.domain.feature.setting.sync


interface UserSettingsRealtimeSyncScheduler {
    suspend fun startListening()
    fun stopListening()
}