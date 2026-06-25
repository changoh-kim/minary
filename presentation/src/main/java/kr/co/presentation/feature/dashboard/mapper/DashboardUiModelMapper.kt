package kr.co.presentation.feature.dashboard.mapper

import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.presentation.feature.dashboard.mapper.DashboardDiaryUiModelMapper.toDashboardDiaryUiModel
import kr.co.presentation.feature.dashboard.model.DashboardUiModel

object DashboardUiModelMapper {
    fun Dashboard.toDashboardUiModel() = DashboardUiModel(
        recentDiaries = recentDiaries.map { it?.toDashboardDiaryUiModel() },
        totalDiaryCount = totalDiaryCount,
        weeklyDiaryCount = weeklyDiaryCount,
        totalWordCount = totalWordCount,
        longestStreak = longestStreak,
        emotionCounts = emotionCounts
    )
}