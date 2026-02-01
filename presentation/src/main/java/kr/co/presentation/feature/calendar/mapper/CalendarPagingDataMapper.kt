package kr.co.presentation.feature.calendar.mapper

import androidx.paging.PagingData
import androidx.paging.insertSeparators
import kr.co.presentation.feature.calendar.model.CalendarGridItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.model.CalendarYearItem
import kr.co.presentation.feature.calendar.model.year


/**
 * PagingData<CalendarMonthItem> 스트림에 연도 구분자(CalendarYearItem)를 삽입하는 확장 함수.
 */
fun PagingData<CalendarMonthItem>.insertYearSeparators(): PagingData<CalendarGridItem> {
    return this.insertSeparators { before: CalendarMonthItem?, after: CalendarMonthItem? ->
        // after가 null이면 리스트의 끝
        if (after == null) {
            return@insertSeparators null
        }
        // before가 null이면 리스트의 시작
        if (before == null) {
            return@insertSeparators CalendarYearItem(year = after.year)
        }

        // 이전, 현재 아이템의 연도가 다를 때
        if (before.year != after.year) {
            CalendarYearItem(year = after.year)
        } else {
            // 같은 연도 내에서는 아무것도 추가하지 않음
            null
        }
    }
}