package kr.co.domain.feature.dashboard.model

import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.emotion.model.Emotion

/**
 * 대시보드 통계 데이터를 담는 도메인 모델
 */
data class Dashboard(
    val recentDiaries: List<Diary?> = emptyList(),      // 연속 작성된 일기 목록(히트맵/스트릭 표시용)
    val totalDiaryCount: Int = 0,                       // 총 일기 개수
    val totalWordCount: Int = 0,                        // 총 단어 수
    val mostFrequentEmotion: Emotion = Emotion.UNKNOWN, // 가장 많이 기록된 감정 (풍부한 감정)
    val leastFrequentEmotion: Emotion = Emotion.UNKNOWN // 가장 적게 기록된 감정 (희소한 감정)
)