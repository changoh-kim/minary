package kr.co.presentation.feature.calendar.mapper

import kr.co.domain.feature.diary.model.Diary
import kr.co.presentation.feature.calendar.model.CalendarDiaryUiModel

object CalendarDiaryUiMapper {
    fun Diary.toCalendarDiaryUiModel() = CalendarDiaryUiModel(
        date = date,
        title = title,
        content = content,
        emotions = emotions,
        imageUrls = imageUrls,
    )
}