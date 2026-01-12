package kr.co.domain.model.calendar.day

import java.time.LocalDate


data class InactiveDayData(
    override val date: LocalDate = LocalDate.now(),
) : DayData