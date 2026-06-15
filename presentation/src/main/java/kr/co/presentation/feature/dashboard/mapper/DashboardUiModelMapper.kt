package kr.co.presentation.feature.dashboard.mapper

import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.presentation.feature.dashboard.model.DashboardUiModel
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiary
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel


object DashboardUiModelMapper {

    fun Dashboard.toDashboardUiModel() = DashboardUiModel(
        recentDiaries = recentDiaries.map { it?.toDiaryUiModel() },
        totalDiaryCount = totalDiaryCount,
        weeklyDiaryCount = weeklyDiaryCount,
        totalWordCount = totalWordCount,
        longestStreak = longestStreak,
        emotionCounts = emotionCounts
    )

    fun DashboardUiModel.toDashboard() = Dashboard(
        recentDiaries = recentDiaries.map { it?.toDiary() },
        totalDiaryCount = totalDiaryCount,
        weeklyDiaryCount = weeklyDiaryCount,
        totalWordCount = totalWordCount,
        longestStreak = longestStreak,
        emotionCounts = emotionCounts
    )
}