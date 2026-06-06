package kr.co.domain.feature.diary.model

import kr.co.domain.feature.diary.model.DiarySyncStatus
import kr.co.domain.feature.emotion.model.Emotion
import java.time.LocalDate
import java.util.UUID


data class Diary(
    val id: String = UUID.randomUUID().toString(),

    val date: LocalDate = LocalDate.now(),
    val title: String = "",
    val content: String = "",
    val emotions: List<Emotion> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: DiarySyncStatus = DiarySyncStatus.PENDING_CREATE,
)