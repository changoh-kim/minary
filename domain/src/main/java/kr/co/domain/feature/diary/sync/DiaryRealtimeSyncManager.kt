package kr.co.domain.feature.diary.sync


interface DiaryRealtimeSyncManager {
    suspend fun startListening()
    fun stopListening()
}