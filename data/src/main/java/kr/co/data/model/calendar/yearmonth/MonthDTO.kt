package kr.co.data.model.calendar.yearmonth

import kr.co.data.model.calendar.day.DayDTO
import java.time.YearMonth


data class MonthDTO(
    val yearMonth: YearMonth = YearMonth.now(),
    val days: List<DayDTO> = emptyList(),
) : YearMonthDTO
