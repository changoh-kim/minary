package kr.co.data.feature.dashboard.mapper

import kr.co.data.feature.dashboard.model.DashboardDto
import kr.co.domain.feature.dashboard.model.Dashboard


object DashboardMapper {

    fun DashboardDto.toDashboard(): Dashboard {
        return Dashboard(
            recentEmotions = this.recentEmotions,
            totalDiaries = this.totalDiaries,
            totalWords = this.totalWords,
            dominantEmotion = this.dominantEmotion,
            rarestEmotion = this.rarestEmotion
        )
    }

    fun Dashboard.toDashboardDto(): DashboardDto {
        return DashboardDto(
            recentEmotions = this.recentEmotions,
            totalDiaries = this.totalDiaries,
            totalWords = this.totalWords,
            dominantEmotion = this.dominantEmotion,
            rarestEmotion = this.rarestEmotion
        )
    }
}