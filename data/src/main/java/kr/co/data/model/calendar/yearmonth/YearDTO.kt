package kr.co.data.model.calendar.yearmonth

import java.time.Year


data class YearDTO(
    val year: Year = Year.now(),
) : YearMonthDTO