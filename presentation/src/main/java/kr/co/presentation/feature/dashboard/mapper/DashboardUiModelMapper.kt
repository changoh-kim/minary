package kr.co.presentation.feature.dashboard.mapper

import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.presentation.feature.dashboard.model.DashboardUiModel


object DashboardUiModelMapper {
    fun Dashboard.toDashboardUiModel() = DashboardUiModel(
        recentEmotions = recentEmotions,
        totalDiaries = totalDiaries,
        totalWords = totalWords,
        dominantEmotion = dominantEmotion,
        rarestEmotion = rarestEmotion
    )

    fun DashboardUiModel.toDashboard() = Dashboard(
        recentEmotions = recentEmotions,
        totalDiaries = totalDiaries,
        totalWords = totalWords,
        dominantEmotion = dominantEmotion,
        rarestEmotion = rarestEmotion
    )
}