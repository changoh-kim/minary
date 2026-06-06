package kr.co.domain.feature.diary.service.sync


interface DiaryRealtimeSyncManager {
    suspend fun startListening()
    fun stopListening()
}