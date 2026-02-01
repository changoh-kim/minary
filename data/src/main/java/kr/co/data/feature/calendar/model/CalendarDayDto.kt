package kr.co.data.feature.calendar.model

import java.time.LocalDate


data class CalendarDayDto(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
)