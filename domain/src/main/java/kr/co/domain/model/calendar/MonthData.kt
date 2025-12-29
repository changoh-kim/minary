package kr.co.domain.model.calendar

import kr.co.domain.extensions.isAfterCurrentMonth
import kr.co.domain.extensions.isCurrentMonth
import kr.co.domain.model.calendar.date.BaseDateData
import kr.co.domain.model.calendar.date.CalendarDateData
import kr.co.domain.model.calendar.date.InactiveDateData
import java.time.YearMonth


data class MonthData(
    override val year: Int,
    val month: Int,
    override val key: String = "${year}-${month}",
    override val contentType: CalendarItem.ContentType = CalendarItem.ContentType.MONTH,
) : CalendarItem {

    companion object {
        // 7일 x 6주
        const val DAY_SIZE = 42
    }

    val days by lazy {
        createDays(year, month)
    }
}

fun MonthData.createYearMonth(): YearMonth {
    return YearMonth.of(year, month)
}

fun MonthData.isCurrentMonth(): Boolean {
    return createYearMonth().isCurrentMonth()
}

fun MonthData.isAfterCurrentMonth(): Boolean {
    return createYearMonth().isAfterCurrentMonth()
}

fun MonthData.createDays(year: Int, month: Int): List<BaseDateData> {
    val yearMonth = YearMonth.of(year, month)
    val monthDays = ArrayList<BaseDateData>(MonthData.Companion.DAY_SIZE)

    // 이전 달 날짜 계산
    // firstDayOfWeek : yearMonth의 1일이 무슨 요일인지 정수로 반환받음 -> 월(1) ~ 일(7)
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value
    // precedingDaysCount -> 일(0) ~ 토(6)
    val precedingDaysCount = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
    if (precedingDaysCount > 0) {
        val previousMonth = yearMonth.minusMonths(1)
        val prevMonthLength = previousMonth.lengthOfMonth()
        val startDay = prevMonthLength - precedingDaysCount + 1
        for (date in startDay..prevMonthLength) {
            monthDays.add(InactiveDateData(date = date))
        }
    }

    // 현재 달 날짜 계산
    val lengthOfMonth = yearMonth.lengthOfMonth()
    for (date in 1..lengthOfMonth) {
        monthDays.add(
            CalendarDateData(
                year = year,
                month = month,
                date = date,
                icon = "\uD83D\uDE02",
            )
        )
    }

    // 다음 달 날짜 계산 (남은 공간 채우기)
    val remaining = MonthData.Companion.DAY_SIZE - monthDays.size
    for (date in 1..remaining) {
        monthDays.add(InactiveDateData(date = date))
    }

    return monthDays
}
