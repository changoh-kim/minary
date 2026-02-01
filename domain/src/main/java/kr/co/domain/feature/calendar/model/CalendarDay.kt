package kr.co.domain.feature.calendar.model

import java.time.LocalDate


data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
)