package kr.co.data.model.calendar.day

import java.time.LocalDate


data class ActiveDayDTO(
    override val date: LocalDate = LocalDate.now(),
    val icon: String = "",
) : DayDTO