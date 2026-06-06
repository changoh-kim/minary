package kr.co.domain.feature.profile.service.sync


interface UserProfileRealtimeSyncScheduler {
    suspend fun startListening()
    fun stopListening()
}