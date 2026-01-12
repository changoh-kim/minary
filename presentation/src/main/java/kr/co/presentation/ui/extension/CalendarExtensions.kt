package kr.co.presentation.ui.extension

import kr.co.presentation.ui.model.calendar.day.DayItem
import kr.co.presentation.ui.model.calendar.yearmonth.MonthItem
import kr.co.presentation.ui.model.calendar.yearmonth.YearItem
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth


// java class extensions
fun Year.isCurrentYear(): Boolean {
    return this == Year.now()
}

fun Year.isAfterCurrentYear(): Boolean {
    return this.isAfter(Year.now())
}

fun YearMonth.isCurrentYearMonth(): Boolean {
    return this == YearMonth.now()
}

fun YearMonth.isAfterCurrentYearMonth(): Boolean {
    return this.isAfter(YearMonth.now())
}

fun LocalDate.isToday(): Boolean {
    return this == LocalDate.now()
}

fun LocalDate.isAfterToday(): Boolean {
    return this.isAfter(LocalDate.now())
}

// minary class extensions
fun YearItem.isCurrentYear(): Boolean {
    return year == Year.now()
}

fun YearItem.isAfterCurrentYear(): Boolean {
    return year.isAfter(Year.now())
}

fun MonthItem.isCurrentMonth(): Boolean {
    return yearMonth == YearMonth.now()
}

fun MonthItem.isAfterCurrentYearMonth(): Boolean {
    return yearMonth.isAfter(YearMonth.now())
}

fun DayItem.isToday(): Boolean {
    return date == LocalDate.now()
}

fun DayItem.isAfterToday(): Boolean {
    return date.isAfter(LocalDate.now())
}