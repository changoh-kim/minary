package kr.co.data.model.calendar.day

import java.time.LocalDate


sealed interface DayDTO {
    val date: LocalDate
}