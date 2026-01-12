package kr.co.domain.model.calendar.yearmonth

import kr.co.domain.model.calendar.day.DayData
import java.time.YearMonth


data class MonthData(
    val yearMonth: YearMonth = YearMonth.now(),
    val days: List<DayData> = emptyList(),
) : YearMonthData