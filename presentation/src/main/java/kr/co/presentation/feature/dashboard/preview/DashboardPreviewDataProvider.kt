package kr.co.presentation.feature.dashboard.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import kr.co.domain.feature.emotion.Emotion
import kr.co.presentation.feature.dashboard.model.DashboardUiModel


internal class DashboardPreviewDataProvider : PreviewParameterProvider<DashboardUiModel> {

    override val values: Sequence<DashboardUiModel> = sequenceOf(
        DashboardUiModel(
            recentEmotions = persistentListOf(
                Emotion.JOY,
                Emotion.JOY,
                Emotion.JOY,
                Emotion.EXCITEMENT,
                Emotion.EXCITEMENT,
                Emotion.TRIUMPH,
            ),
            totalDiaries = 100,
            totalWords = 10000,
            dominantEmotion = Emotion.JOY,
            rarestEmotion = Emotion.TRIUMPH
        ),
    )
}