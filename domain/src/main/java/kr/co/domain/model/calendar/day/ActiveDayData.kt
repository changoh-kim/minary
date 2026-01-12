package kr.co.domain.model.calendar.day

import java.time.LocalDate


data class ActiveDayData(
    override val date: LocalDate = LocalDate.now(),
    val icon: String = "",
) : DayData