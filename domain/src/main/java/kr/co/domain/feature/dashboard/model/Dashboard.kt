package kr.co.domain.feature.dashboard.model

import kr.co.domain.feature.emotion.Emotion


/**
 * 대시보드 통계 데이터를 담는 도메인 모델
 */
data class Dashboard(
    // 연속 작성된 일기의 감정 목록 (히트맵/스트릭 표시용)
    val recentEmotions: List<Emotion> = emptyList(),
    // 총 일기 개수
    val totalDiaries: Int = 0,
    // 총 단어 수
    val totalWords: Int = 0,
    // 가장 많이 기록된 감정 (풍부한 감정)
    val dominantEmotion: Emotion = Emotion.UNKNOWN,
    // 가장 적게 기록된 감정 (희소한 감정)
    val rarestEmotion: Emotion = Emotion.UNKNOWN
)