package kr.co.domain.feature.calendar.model

import java.time.YearMonth


data class CalendarMonth(
    val yearMonth: YearMonth,
    val days: List<CalendarDay>
)