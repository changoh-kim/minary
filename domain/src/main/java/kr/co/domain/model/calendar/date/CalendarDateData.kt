package kr.co.domain.model.calendar.date

import kr.co.domain.extensions.isAfterToday
import kr.co.domain.extensions.isToday
import java.time.LocalDate


data class CalendarDateData(
    val year: Int,
    val month: Int,
    override val date: Int,
    val icon: String,
) : BaseDateData

fun CalendarDateData.createLocalDate(): LocalDate {
    return LocalDate.of(year, month, date)
}

fun CalendarDateData.isToday(): Boolean {
    return createLocalDate().isToday()
}

fun CalendarDateData.isAfterToday(): Boolean {
    return createLocalDate().isAfterToday()
}