package kr.co.presentation.feature.dashboard.screen.dashboard.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.core.common.model.Emotion
import kr.co.presentation.feature.dashboard.model.DashboardDiaryUiModel
import kr.co.presentation.feature.dashboard.model.DashboardUiModel
import java.time.LocalDate
import java.util.Random

class DashboardScreenPreviewParameterProvider :
    PreviewParameterProvider<DashboardUiModel> {

    override val values: Sequence<DashboardUiModel> = sequenceOf(
        DashboardUiModel(
            recentDiaries = run {
                val random = Random(42) // 고정된 시드값 사용
                val allEmotions = Emotion.entries
                (0..90).map { dayOffset ->
                    if (dayOffset % 7 == 3 || dayOffset % 11 == 0) {
                        null
                    } else {
                        // 고정된 시드를 사용하여 항상 동일한 결과 생성
                        val count = random.nextInt(5) + 1
                        val emotions = (1..count).map {
                            allEmotions[random.nextInt(allEmotions.size)]
                        }.distinct()

                        DashboardDiaryUiModel(
                            date = LocalDate.now().minusDays(dayOffset.toLong()),
                            emotions = emotions
                        )
                    }
                }.reversed()
            },
            totalDiaryCount = 128,
            weeklyDiaryCount = 5,
            totalWordCount = 3450,
            longestStreak = 15,
            emotionCounts = mapOf(
                Emotion.JOY to 24,
                Emotion.SATISFACTION to 18,
                Emotion.CALMNESS to 15,
                Emotion.BOREDOM to 12,
                Emotion.ANXIETY to 8
            )
        ),
        //DashboardUiModel(), // Empty
    )
}
