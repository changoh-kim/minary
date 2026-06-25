package kr.co.domain.feature.dashboard.model

import kr.co.domain.feature.diary.model.Diary
import kr.co.core.common.model.Emotion

/**
 * 대시보드 통계 데이터를 담는 도메인 모델
 */
data class Dashboard(
    val recentDiaries: List<Diary?> = emptyList(),      // 연속 작성된 일기 목록(히트맵/스트릭 표시용)
    val totalDiaryCount: Int = 0,                       // 총 일기 개수
    val weeklyDiaryCount: Int = 0,                      // 이번 주 일기 개수
    val totalWordCount: Int = 0,                        // 총 단어 수
    val longestStreak: Int = 0,                         // 최장 연속 작성일
    val emotionCounts: Map<Emotion, Int> = emptyMap()   // 감정별 작성 횟수
)