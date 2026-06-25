package kr.co.presentation.feature.dashboard.mapper

import kr.co.domain.feature.diary.model.Diary
import kr.co.presentation.feature.dashboard.model.DashboardDiaryUiModel

object DashboardDiaryUiModelMapper {
    fun Diary.toDashboardDiaryUiModel() = DashboardDiaryUiModel(
        date = date,
        emotions = emotions,
    )
}