package kr.co.presentation.ui.preview

import kr.co.domain.model.calendar.CalendarItem
import kr.co.domain.model.calendar.MonthData
import kr.co.domain.model.calendar.YearData
import java.time.Year
import java.time.YearMonth


object CalendarPreviewData {
    const val COUNT_OF_MONTH = 12

    fun createMonthDataList(targetYearMonth: YearMonth = YearMonth.now()): List<MonthData> {
        return List(COUNT_OF_MONTH) { index ->
            val yearMonth = targetYearMonth.minusMonths(index.toLong())
            MonthData(year = yearMonth.year, month = yearMonth.monthValue)
        }
    }

    const val COUNT_OF_CALENDAR_ITEM = 13

    const val MONTH_OCTOBER_10 = 10
    const val MONTH_JULY_7 = 7
    const val MONTH_APRIL_4 = 4
    const val MONTH_JANUARY_1 = 1

    fun createYearlyCalendarItems(year: Int = Year.now().value): List<CalendarItem> {
        val result = ArrayList<CalendarItem>(COUNT_OF_CALENDAR_ITEM)
        val quarterStartMonths =
            intArrayOf(
                MONTH_OCTOBER_10,
                MONTH_JULY_7,
                MONTH_APRIL_4,
                MONTH_JANUARY_1
            )

        for (startMonth in quarterStartMonths) {
            // 분기별 월의 개수 - 3개
            for (i in 0..2) {
                result.add(MonthData(year = year, month = startMonth + i))
            }
        }
        result.add(YearData(year))
        return result
    }
}
