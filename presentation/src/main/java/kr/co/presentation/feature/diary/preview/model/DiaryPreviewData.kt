package kr.co.presentation.feature.diary.preview.model

import androidx.compose.runtime.Immutable
import kr.co.domain.feature.emotion.model.Emotion
import kr.co.presentation.feature.diary.viewmodel.DiaryScreenMode


@Immutable
data class DiaryPreviewData(
    val screenMode: DiaryScreenMode = DiaryScreenMode.Edit,
    val emotion: Emotion = Emotion.UNKNOWN,
)