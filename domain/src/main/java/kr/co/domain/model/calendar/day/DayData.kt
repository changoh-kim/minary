package kr.co.domain.model.calendar.day

import java.time.LocalDate


sealed interface DayData {
    val date: LocalDate
}