package kr.co.presentation.feature.dashboard.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.domain.feature.emotion.model.Emotion
import kr.co.presentation.feature.dashboard.model.DashboardUiModel
import kr.co.presentation.feature.diary.model.DiaryUiModel
import java.time.LocalDate


internal class DashboardPreviewDataProvider : PreviewParameterProvider<DashboardUiModel> {

    override val values: Sequence<DashboardUiModel> = sequenceOf(
        DashboardUiModel(
            recentDiaries = listOf(
                DiaryUiModel(date = LocalDate.now()),
                DiaryUiModel(date = LocalDate.now().minusDays(1)),
                DiaryUiModel(date = LocalDate.now().minusDays(2)),
                null,                                       //3
                DiaryUiModel(date = LocalDate.now().minusDays(4)),
                DiaryUiModel(date = LocalDate.now().minusDays(5)),
                null,                                       //6
                DiaryUiModel(date = LocalDate.now().minusDays(7))
            ),
            totalDiaryCount = 100,
            totalWordCount = 10000,
            mostFrequentEmotion = Emotion.JOY,
            leastFrequentEmotion = Emotion.TRIUMPH
        ),
        DashboardUiModel(), // Empty
    )
}