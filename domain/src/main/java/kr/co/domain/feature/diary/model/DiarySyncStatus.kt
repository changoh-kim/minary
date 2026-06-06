package kr.co.domain.feature.diary.model

enum class DiarySyncStatus {
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    SYNCED,
}