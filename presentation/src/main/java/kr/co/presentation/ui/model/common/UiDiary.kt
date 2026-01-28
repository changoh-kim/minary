package kr.co.presentation.ui.model.common

import androidx.compose.runtime.Immutable
import kr.co.domain.model.emotion.Emotion
import java.time.LocalDate


@Immutable
data class UiDiary(
    val id: Long = 0L,
    val date: LocalDate = LocalDate.now(),
    val title: String = "",
    val content: String = "",
    val emotion: Emotion = Emotion.UNKNOWN,
)