package kr.co.presentation.feature.diary.preview.model

import androidx.compose.runtime.Immutable
import kr.co.domain.feature.emotion.Emotion
import kr.co.presentation.feature.diary.viewmodel.DiaryScreenMode


@Immutable
data class DiaryPreviewData(
    val emotion: Emotion,
    val screenMode: DiaryScreenMode
)