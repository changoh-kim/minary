package kr.co.data.feature.calendar.mapper

import kr.co.data.feature.calendar.model.CalendarDayDto
import kr.co.data.feature.calendar.model.CalendarMonthDto
import kr.co.domain.feature.calendar.model.CalendarDay
import kr.co.domain.feature.calendar.model.CalendarMonth


object CalendarDataMapper {

    fun CalendarMonthDto.toCalendarMonth(): CalendarMonth {
        return CalendarMonth(
            yearMonth = this.yearMonth,
            days = this.days.map { it.toCalendarDay() },
        )
    }

    fun CalendarMonth.toCalendarMonthDto(): CalendarMonthDto {
        return CalendarMonthDto(
            yearMonth = this.yearMonth,
            days = this.days.map { it.toCalendarDayDto() },
        )
    }

    fun CalendarDayDto.toCalendarDay(): CalendarDay {
        return CalendarDay(
            date = this.date,
            isCurrentMonth = this.isCurrentMonth
        )
    }

    fun CalendarDay.toCalendarDayDto(): CalendarDayDto {
        return CalendarDayDto(
            date = this.date,
            isCurrentMonth = this.isCurrentMonth
        )
    }
}