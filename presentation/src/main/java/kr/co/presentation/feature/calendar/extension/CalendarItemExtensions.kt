package kr.co.presentation.feature.calendar.extension

import kr.co.presentation.feature.calendar.model.CalendarDayItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.model.CalendarYearItem
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

fun CalendarYearItem.isCurrentYear(): Boolean = year == Year.now()
fun CalendarYearItem.isAfterCurrentYear(): Boolean = year.isAfter(Year.now())
fun CalendarMonthItem.isCurrentMonth(): Boolean = yearMonth == YearMonth.now()
fun CalendarMonthItem.isAfterCurrentYearMonth(): Boolean = yearMonth.isAfter(YearMonth.now())
fun CalendarDayItem.isToday(): Boolean = date == LocalDate.now()
fun CalendarDayItem.isAfterToday(): Boolean = date.isAfter(LocalDate.now())
fun CalendarDayItem.toYear(): Year = Year.of(date.year)
fun CalendarDayItem.toYearMonth(): YearMonth = YearMonth.of(date.year, date.monthValue)