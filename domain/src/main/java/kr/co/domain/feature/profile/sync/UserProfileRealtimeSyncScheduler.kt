package kr.co.domain.feature.profile.sync


interface UserProfileRealtimeSyncScheduler {
    suspend fun startListening()
    fun stopListening()
}