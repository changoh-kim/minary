package kr.co.presentation.feature.diary.preview.model

import androidx.compose.runtime.Immutable
import kr.co.domain.feature.emotion.model.Emotion


@Immutable
data class DiaryPreviewData(
    val emotion: Emotion = Emotion.UNKNOWN,
)