package kr.co.data.source

import kr.co.data.model.calendar.day.ActiveDayDTO
import kr.co.data.model.calendar.day.DayDTO
import kr.co.data.model.calendar.day.InactiveDayDTO
import kr.co.data.model.calendar.yearmonth.MonthDTO
import kr.co.data.model.calendar.yearmonth.YearDTO
import kr.co.data.model.calendar.yearmonth.YearMonthDTO
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import javax.inject.Inject

/**
 * 캘린더 관련 데이터 생성을 담당하는 클래스입니다.
 * Dagger를 통해 의존성 주입으로 관리됩니다.
 */
class CalendarLocalDataSource @Inject constructor(
) {
    companion object {
        // 년도 헤더 + 12 개월
        const val COUNT_OF_CALENDAR_ITEM = 13

        const val MONTH_OCTOBER_10 = 10
        const val MONTH_JULY_7 = 7
        const val MONTH_APRIL_4 = 4
        const val MONTH_JANUARY_1 = 1

        // 7일 x 6주
        const val COUNT_OF_ALL_DAYS = 42
    }

    /**
     * 그리드 뷰에 표시할 달력 아이템 리스트를 생성합니다.
     * 생성 순서는 10월부터 역순으로 분기별로 생성됩니다 (10-12, 7-9, 4-6, 1-3).
     * 리스트의 마지막에는 연도 아이템이 추가됩니다.
     *
     * @param year 대상 연도.
     * @return 연도 및 월별 데이터 리스트.
     */
    fun createCalendarOf(year: Year): List<YearMonthDTO> {
        val result = ArrayList<YearMonthDTO>(COUNT_OF_CALENDAR_ITEM)
        val quarterStartMonths =
            intArrayOf(
                MONTH_OCTOBER_10,
                MONTH_JULY_7,
                MONTH_APRIL_4,
                MONTH_JANUARY_1,
            )

        // 분기별 월 데이터 생성
        for (startMonth in quarterStartMonths) {
            for (i in 0..2) {
                val monthValue = startMonth + i
                result.add(createMonth(YearMonth.of(year.value, monthValue)))
            }
        }

        result.add(YearDTO(year))
        return result
    }

    /**
     * 특정 연월에 해당하는 월 데이터를 생성합니다.
     *
     * @param yearMonth 대상 연월.
     * @return 생성된 월 데이터.
     */
    fun createMonth(yearMonth: YearMonth): MonthDTO {
        return MonthDTO(
            yearMonth = yearMonth,
            days = createDays(yearMonth)
        )
    }

    /**
     * 특정 연월의 날짜 데이터 리스트를 생성합니다.
     * 이전 달, 현재 달, 다음 달의 날짜를 포함하여 항상 42개의 날짜 데이터를 생성합니다.
     *
     * @param yearMonth 대상 연월.
     * @return 42개의 날짜 데이터 리스트.
     */
    private fun createDays(yearMonth: YearMonth): List<DayDTO> {
        val monthDays = ArrayList<DayDTO>(COUNT_OF_ALL_DAYS)

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
                monthDays.add(
                    InactiveDayDTO(
                        date = LocalDate.of(previousMonth.year, previousMonth.monthValue, date),
                    )
                )
            }
        }

        // 현재 달 날짜 계산
        val lengthOfMonth = yearMonth.lengthOfMonth()
        for (date in 1..lengthOfMonth) {
            monthDays.add(
                ActiveDayDTO(
                    date = LocalDate.of(yearMonth.year, yearMonth.monthValue, date),
                    icon = "😂",
                )
            )
        }

        // 다음 달 날짜 계산 (남은 공간 채우기)
        val nextMonth = yearMonth.plusMonths(1)
        val remaining = COUNT_OF_ALL_DAYS - monthDays.size
        for (date in 1..remaining) {
            monthDays.add(
                InactiveDayDTO(
                    date = LocalDate.of(nextMonth.year, nextMonth.monthValue, date),
                )
            )
        }

        return monthDays
    }
}
