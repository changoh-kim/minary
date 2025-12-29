package kr.co.domain.extensions

import java.time.LocalDate
import java.time.Year
import java.time.YearMonth


fun Year.isCurrentYear(): Boolean = this == Year.now()
fun Year.isAfterCurrentYear(): Boolean = this.isAfter(Year.now())

fun YearMonth.isCurrentMonth(): Boolean = this == YearMonth.now()
fun YearMonth.isAfterCurrentMonth(): Boolean = this.isAfter(YearMonth.now())

fun LocalDate.isToday(): Boolean = this == LocalDate.now()
fun LocalDate.isAfterToday(): Boolean = this.isAfter(LocalDate.now())