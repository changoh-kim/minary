package kr.co.presentation.feature.diary.screen.detail.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.core.common.model.Emotion
import kr.co.presentation.feature.diary.preview.DiaryPreviewData

internal class DiaryDetailPreviewParameterProvider :
    PreviewParameterProvider<DiaryPreviewData> {

    override val values: Sequence<DiaryPreviewData> = sequenceOf(
        DiaryPreviewData(
            emotion = Emotion.EXCITEMENT
        ),
        DiaryPreviewData(
            emotion = Emotion.ENTRANCEMENT
        ),
    )
}