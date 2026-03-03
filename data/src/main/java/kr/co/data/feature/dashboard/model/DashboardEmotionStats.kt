package kr.co.data.feature.dashboard.model


/**
 * 대시보드에서 감정별 빈도수를 집계하기 위한 데이터 모델
 */
data class DashboardEmotionStats(
    val emotion: String,
    val count: Int
)