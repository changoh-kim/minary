package kr.co.data.model.calendar.day

import java.time.LocalDate


data class InactiveDayDTO(
    override val date: LocalDate = LocalDate.now(),
) : DayDTO
