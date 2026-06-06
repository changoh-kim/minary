package kr.co.data.feature.dashboard.mapper

import kr.co.data.feature.dashboard.model.DashboardModel
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiary
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiaryWithRelations
import kr.co.domain.feature.dashboard.model.Dashboard

object DashboardMapper {
    fun DashboardModel.toDashboard(): Dashboard {
        return Dashboard(
            recentDiaries = recentDiaries.map { it?.toDiary() },
            totalDiaryCount = totalDiaryCount,
            totalWordCount = totalWordCount,
            mostFrequentEmotion = mostFrequentEmotion,
            leastFrequentEmotion = leastFrequentEmotion
        )
    }

    fun Dashboard.toDashboardModel(): DashboardModel {
        return DashboardModel(
            recentDiaries = recentDiaries.map { it?.toDiaryWithRelations() },
            totalDiaryCount = totalDiaryCount,
            totalWordCount = totalWordCount,
            mostFrequentEmotion = mostFrequentEmotion,
            leastFrequentEmotion = leastFrequentEmotion
        )
    }
}