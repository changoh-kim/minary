package kr.co.data.feature.dashboard.model


/**
 * 대시보드에서 일기 수와 단어 수를 집계하기 위한 데이터 모델
 */
data class DashboardCountStats(
    val totalDiaries: Int,
    val totalWords: Int
)