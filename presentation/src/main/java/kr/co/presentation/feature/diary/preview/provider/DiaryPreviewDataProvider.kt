package kr.co.presentation.feature.diary.preview.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.domain.feature.emotion.model.Emotion
import kr.co.presentation.feature.diary.preview.model.DiaryPreviewData
import kr.co.presentation.feature.diary.viewmodel.DiaryScreenMode


internal class DiaryPreviewDataProvider : PreviewParameterProvider<DiaryPreviewData> {

    override val values: Sequence<DiaryPreviewData> = sequenceOf(
        DiaryPreviewData(
            screenMode = DiaryScreenMode.Edit,
            emotion = Emotion.EXCITEMENT
        ),
        DiaryPreviewData(
            screenMode = DiaryScreenMode.Preview,
            emotion = Emotion.ENTRANCEMENT
        ),
    )
}