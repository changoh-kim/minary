package kr.co.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import kr.co.domain.model.calendar.day.ActiveDayData
import kr.co.domain.model.calendar.day.DayData
import kr.co.domain.model.calendar.day.InactiveDayData
import kr.co.domain.model.calendar.yearmonth.MonthData
import kr.co.domain.model.calendar.yearmonth.YearData
import kr.co.domain.model.calendar.yearmonth.YearMonthData
import kr.co.presentation.ui.model.calendar.day.ActiveDayItem
import kr.co.presentation.ui.model.calendar.day.DayItem
import kr.co.presentation.ui.model.calendar.day.InactiveDayItem
import kr.co.presentation.ui.model.calendar.yearmonth.MonthItem
import kr.co.presentation.ui.model.calendar.yearmonth.YearItem
import kr.co.presentation.ui.model.calendar.yearmonth.YearMonthItem


object CalendarItemMapper {
    fun YearMonthData.toYearMonthItem(): YearMonthItem {
        return when (this) {
            is YearData -> this.toYearItem()
            is MonthData -> this.toMonthItem()
        }
    }

    private fun YearData.toYearItem(): YearItem {
        return YearItem(
            year = this.year,
        )
    }

    fun MonthData.toMonthItem(): MonthItem {
        return MonthItem(
            yearMonth = this.yearMonth,
            days = this.days.map { it.toDayItem() }.toImmutableList(),
        )
    }

    private fun DayData.toDayItem(): DayItem {
        return when (this) {
            is ActiveDayData -> ActiveDayItem(
                date = date,
                icon = icon,
            )

            is InactiveDayData -> InactiveDayItem(
                date = date,
            )
        }
    }
}