package kr.co.presentation.feature.calendar.extension

import kr.co.presentation.feature.calendar.model.CalendarDayItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.model.CalendarYearItem
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
fun CalendarYearItem.isCurrentYear(): Boolean = year == Year.now()
fun CalendarYearItem.isAfterCurrentYear(): Boolean = year.isAfter(Year.now())
fun CalendarMonthItem.isCurrentMonth(): Boolean = yearMonth == YearMonth.now()
fun CalendarMonthItem.isAfterCurrentYearMonth(): Boolean = yearMonth.isAfter(YearMonth.now())
fun CalendarDayItem.isToday(): Boolean = date == LocalDate.now()
fun CalendarDayItem.isAfterToday(): Boolean = date.isAfter(LocalDate.now())
fun CalendarDayItem.toYear(): Year = Year.of(date.year)
fun CalendarDayItem.toYearMonth(): YearMonth = YearMonth.of(date.year, date.monthValue)