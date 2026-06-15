package kr.co.data.feature.dashboard.model

import kr.co.data.feature.diary.source.local.model.DiaryWithRelations
import kr.co.domain.feature.emotion.model.Emotion

data class DashboardModel(
    val recentDiaries: List<DiaryWithRelations?> = emptyList(),
    val totalDiaryCount: Int = 0,
    val weeklyDiaryCount: Int = 0,
    val totalWordCount: Int = 0,
    val longestStreak: Int = 0,
    val emotionCounts: Map<Emotion, Int> = emptyMap()
)