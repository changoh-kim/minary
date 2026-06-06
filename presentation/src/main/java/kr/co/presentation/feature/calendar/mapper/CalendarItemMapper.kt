package kr.co.presentation.feature.calendar.mapper

import kotlinx.collections.immutable.toImmutableList
import kr.co.domain.feature.calendar.model.CalendarDay
import kr.co.domain.feature.calendar.model.CalendarMonth
import kr.co.presentation.feature.calendar.model.CalendarDayItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel


object CalendarItemMapper {

    fun CalendarMonth.toCalendarMonthItem(): CalendarMonthItem {
        return CalendarMonthItem(
            yearMonth = this.yearMonth,
            days = this.days.map { it.toCalendarDayItem() }.toImmutableList(),
            syncStatus = this.syncStatus
        )
    }

    fun CalendarDay.toCalendarDayItem(): CalendarDayItem {
        return CalendarDayItem(
            date = this.date,
            isCurrentMonth = this.isCurrentMonth,
            diary = this.diary?.toDiaryUiModel()
        )
    }
}