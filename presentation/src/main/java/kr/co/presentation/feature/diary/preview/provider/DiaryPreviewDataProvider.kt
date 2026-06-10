package kr.co.presentation.feature.diary.preview.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.domain.feature.emotion.model.Emotion
import kr.co.presentation.feature.diary.preview.model.DiaryPreviewData


internal class DiaryPreviewDataProvider : PreviewParameterProvider<DiaryPreviewData> {

    override val values: Sequence<DiaryPreviewData> = sequenceOf(
        DiaryPreviewData(
            emotion = Emotion.EXCITEMENT
        ),
        DiaryPreviewData(
            emotion = Emotion.ENTRANCEMENT
        ),
    )
}