package kr.co.presentation.feature.diary.preview

import androidx.compose.runtime.Immutable
import kr.co.core.common.model.Emotion

@Immutable
data class DiaryPreviewData(
    val emotion: Emotion = Emotion.UNKNOWN,
)