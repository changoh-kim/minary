package kr.co.domain.feature.calendar.generator

import kr.co.domain.feature.calendar.model.CalendarDay
import kr.co.domain.feature.calendar.model.CalendarMonth
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import javax.inject.Inject


class CalendarGenerator @Inject constructor() {

    sealed interface SortOrder {
        data object Ascending: SortOrder      //오름차순 (1월, 2월, ..., 12월)
        data object Descending: SortOrder     //내림차순 (12월, 11월, ..., 1월)
    }

    companion object {
        const val START_YEAR_1902 = 1902
        const val START_MONTH_1 = 1
        const val MONTHS_IN_YEAR = 12             // 12 개월
        const val CALENDAR_GRID_DAY_COUNT = 42    // 7일 x 6주
    }

    /**
     * 지정된 연도의 월 데이터를 오름차순 또는 내림차순으로 생성합니다.
     *
     * @param year 대상 연도.
     * @param sortOrder 정렬 순서 (Ascending: 오름차순, Descending: 내림차순). 기본값은 오름차순입니다.
     * @return 정렬된 월 데이터 리스트.
     */
    fun generateMonths(
        year: Year,
        sortOrder: SortOrder = SortOrder.Ascending
    ): List<CalendarMonth> {
        val months = ArrayList<CalendarMonth>(MONTHS_IN_YEAR)
        val yearValue = year.value

        val monthRange = when (sortOrder) {
            is SortOrder.Ascending -> 1..MONTHS_IN_YEAR
            is SortOrder.Descending -> MONTHS_IN_YEAR downTo 1
        }

        for (monthValue in monthRange) {
            months.add(generateMonth(YearMonth.of(yearValue, monthValue)))
        }
        return months
    }

    /**
     * 주어진 연월로부터 이전 12개월간의 월 아이템 리스트를 생성합니다.
     *
     * @param yearMonth 기준 연월.
     * @param sortOrder 정렬 순서 (Ascending: 오름차순, Descending: 내림차순). 기본값은 오름차순입니다.
     * @return 12개월 분의 월 아이템 리스트.
     */
    fun generateMonths(
        yearMonth: YearMonth,
        size: Int = MONTHS_IN_YEAR,
        sortOrder: SortOrder = SortOrder.Ascending
    ): List<CalendarMonth> {
        return when (sortOrder) {
            is SortOrder.Ascending -> {
                val startIndex = size - 1
                List(size) { index ->
                    generateMonth(yearMonth.minusMonths((startIndex - index).toLong()))
                }
            }

            is SortOrder.Descending -> {
                List(size) { index ->
                    generateMonth(yearMonth.minusMonths(index.toLong()))
                }
            }
        }
    }

    /**
     * 특정 연월에 해당하는 월 데이터를 생성합니다.
     *
     * @param yearMonth 대상 연월.
     * @return 생성된 월 데이터.
     */
    fun generateMonth(yearMonth: YearMonth): CalendarMonth {
        return CalendarMonth(
            yearMonth = yearMonth,
            days = generateDays(yearMonth)
        )
    }

    /**
     * 특정 연월의 날짜 데이터 리스트를 생성합니다.
     * 이전 달, 현재 달, 다음 달의 날짜를 포함하여 항상 42개의 날짜 데이터를 생성합니다.
     *
     * @param yearMonth 대상 연월.
     * @return 42개의 날짜 데이터 리스트.
     */
    private fun generateDays(yearMonth: YearMonth): List<CalendarDay> {
        val monthDays = ArrayList<CalendarDay>(CALENDAR_GRID_DAY_COUNT)

        // 이전 달 날짜 계산
        val firstDay = yearMonth.atDay(1)
        // firstDayOfWeek : yearMonth의 1일이 무슨 요일인지 정수로 반환받음 -> 월(1) ~ 일(7)
        val firstDayOfWeek = firstDay.dayOfWeek.value
        // precedingDaysCount -> 일(0) ~ 토(6)
        val precedingDaysCount = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
        if (precedingDaysCount > 0) {
            val previousMonth = yearMonth.minusMonths(1)
            val prevMonthLength = previousMonth.lengthOfMonth()
            val startDay = prevMonthLength - precedingDaysCount + 1
            for (dayOfMonth in startDay..prevMonthLength) {
                val date = LocalDate.of(previousMonth.year, previousMonth.monthValue, dayOfMonth)
                monthDays.add(
                    CalendarDay(date = date, isCurrentMonth = false)
                )
            }
        }

        // 현재 달 날짜 계산
        val lengthOfMonth = yearMonth.lengthOfMonth()
        for (dayOfMonth in 1..lengthOfMonth) {
            val date = LocalDate.of(yearMonth.year, yearMonth.monthValue, dayOfMonth)
            monthDays.add(
                CalendarDay(date = date, isCurrentMonth = true)
            )
        }

        // 다음 달 날짜 계산 (남은 공간 채우기)
        val nextMonth = yearMonth.plusMonths(1)
        val remaining = CALENDAR_GRID_DAY_COUNT - monthDays.size
        for (dayOfMonth in 1..remaining) {
            val date = LocalDate.of(nextMonth.year, nextMonth.monthValue, dayOfMonth)
            monthDays.add(
                CalendarDay(date = date, isCurrentMonth = false)
            )
        }

        return monthDays
    }
}