package kr.co.data.feature.calendar.mapper

import kr.co.data.feature.calendar.mapper.CalendarDayMapper.toCalendarDay
import kr.co.data.feature.calendar.mapper.CalendarDayMapper.toCalendarDayModel
import kr.co.data.feature.calendar.model.CalendarMonthModel
import kr.co.domain.feature.calendar.model.CalendarMonth

object CalendarMonthMapper {
    fun CalendarMonthModel.toCalendarMonth(): CalendarMonth {
        return CalendarMonth(
            yearMonth = yearMonth,
            days = days.map { it.toCalendarDay() },
            syncStatus = syncStatus
        )
    }

    fun CalendarMonth.toCalendarMonthModel(): CalendarMonthModel {
        return CalendarMonthModel(
            yearMonth = yearMonth,
            days = days.map { it.toCalendarDayModel() },
            syncStatus = syncStatus
        )
    }
}