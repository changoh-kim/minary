package kr.co.data.feature.dashboard.model

import kr.co.domain.feature.emotion.Emotion


data class DashboardDto(
    val recentEmotions: List<Emotion> = emptyList(),
    val totalDiaries: Int = 0,
    val totalWords: Int = 0,
    val dominantEmotion: Emotion = Emotion.UNKNOWN,
    val rarestEmotion: Emotion = Emotion.UNKNOWN
)