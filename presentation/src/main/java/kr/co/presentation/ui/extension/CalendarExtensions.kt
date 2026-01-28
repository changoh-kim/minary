package kr.co.presentation.ui.extension

import kr.co.presentation.ui.model.calendar.day.DayItem
import kr.co.presentation.ui.model.calendar.yearmonth.MonthItem
import kr.co.presentation.ui.model.calendar.yearmonth.YearItem
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth


// java class extensions
fun Year.isCurrentYear(): Boolean = this == Year.now()
fun Year.isAfterCurrentYear(): Boolean = this.isAfter(Year.now())
fun YearMonth.isCurrentYearMonth(): Boolean = this == YearMonth.now()
fun YearMonth.isAfterCurrentYearMonth(): Boolean = this.isAfter(YearMonth.now())
fun LocalDate.isToday(): Boolean = this == LocalDate.now()
fun LocalDate.isAfterToday(): Boolean = this.isAfter(LocalDate.now())

// minary class extensions
fun YearItem.isCurrentYear(): Boolean = year == Year.now()
fun YearItem.isAfterCurrentYear(): Boolean = year.isAfter(Year.now())
fun MonthItem.isCurrentMonth(): Boolean = yearMonth == YearMonth.now()
fun MonthItem.isAfterCurrentYearMonth(): Boolean = yearMonth.isAfter(YearMonth.now())
fun DayItem.isToday(): Boolean = date == LocalDate.now()
fun DayItem.isAfterToday(): Boolean = date.isAfter(LocalDate.now())
fun DayItem.toYear(): Year = Year.of(date.year)
fun DayItem.toYearMonth(): YearMonth = YearMonth.of(date.year, date.monthValue)