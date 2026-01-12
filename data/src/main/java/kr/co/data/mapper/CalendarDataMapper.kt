package kr.co.data.mapper

import kr.co.data.model.calendar.day.ActiveDayDTO
import kr.co.data.model.calendar.day.DayDTO
import kr.co.data.model.calendar.day.InactiveDayDTO
import kr.co.data.model.calendar.yearmonth.MonthDTO
import kr.co.data.model.calendar.yearmonth.YearDTO
import kr.co.data.model.calendar.yearmonth.YearMonthDTO
import kr.co.domain.model.calendar.day.ActiveDayData
import kr.co.domain.model.calendar.day.DayData
import kr.co.domain.model.calendar.day.InactiveDayData
import kr.co.domain.model.calendar.yearmonth.MonthData
import kr.co.domain.model.calendar.yearmonth.YearData


object CalendarDataMapper {
    fun YearMonthDTO.toYearMonthData(): kr.co.domain.model.calendar.yearmonth.YearMonthData {
        return when (this) {
            is YearDTO -> this.toYearData()
            is MonthDTO -> this.toMonthData()
        }
    }

    private fun YearDTO.toYearData(): YearData {
        return YearData(
            year = this.year,
        )
    }

    fun MonthDTO.toMonthData(): MonthData {
        return MonthData(
            yearMonth = this.yearMonth,
            days = this.days.map { it.toDayData() },
        )
    }

    private fun DayDTO.toDayData(): DayData {
        return when (this) {
            is ActiveDayDTO -> ActiveDayData(
                date = date,
                icon = icon,
            )

            is InactiveDayDTO -> InactiveDayData(
                date = date,
            )
        }
    }
}