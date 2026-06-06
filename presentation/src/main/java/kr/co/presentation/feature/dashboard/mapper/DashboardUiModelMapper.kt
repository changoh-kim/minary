package kr.co.presentation.feature.dashboard.mapper

import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.presentation.feature.dashboard.model.DashboardUiModel
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiary
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel


object DashboardUiModelMapper {

    fun Dashboard.toDashboardUiModel() = DashboardUiModel(
        recentDiaries = recentDiaries.map { it?.toDiaryUiModel() },
        totalDiaryCount = totalDiaryCount,
        totalWordCount = totalWordCount,
        mostFrequentEmotion = mostFrequentEmotion,
        leastFrequentEmotion = leastFrequentEmotion
    )

    fun DashboardUiModel.toDashboard() = Dashboard(
        recentDiaries = recentDiaries.map { it?.toDiary() },
        totalDiaryCount = totalDiaryCount,
        totalWordCount = totalWordCount,
        mostFrequentEmotion = mostFrequentEmotion,
        leastFrequentEmotion = leastFrequentEmotion
    )
}