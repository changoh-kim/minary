package kr.co.domain.feature.diary.model

import kr.co.domain.feature.emotion.Emotion
import java.time.LocalDate


data class Diary(
    val id: Long = 0L,
    val date: LocalDate = LocalDate.now(),
    val title: String = "",
    val content: String = "",
    val emotion: Emotion = Emotion.UNKNOWN,
    val timestamp: Long = 0L,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
)