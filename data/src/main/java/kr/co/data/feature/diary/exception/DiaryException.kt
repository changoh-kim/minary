package kr.co.data.feature.diary.exception

sealed class DiaryException(message: String) : RuntimeException(message) {
    class NotFoundException(message: String = "Diary not found.") : DiaryException(message)
    // 다이어리가 동기화되지 않음
    // class OutOfSyncException(message: String) : DiaryException(message)
}