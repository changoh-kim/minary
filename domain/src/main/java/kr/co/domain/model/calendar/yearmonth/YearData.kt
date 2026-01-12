package kr.co.domain.model.calendar.yearmonth

import java.time.Year


data class YearData(
    val year: Year = Year.now(),
) : YearMonthData