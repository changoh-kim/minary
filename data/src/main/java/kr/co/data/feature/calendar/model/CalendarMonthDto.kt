package kr.co.data.feature.calendar.model

import java.time.YearMonth


data class CalendarMonthDto(
    val yearMonth: YearMonth,
    val days: List<CalendarDayDto>
)