package kr.co.presentation.ui.model.calendar.yearmonth

import androidx.compose.runtime.Immutable
import kr.co.presentation.ui.model.calendar.BaseItem
import java.time.Year


@Immutable
sealed interface YearMonthItem : BaseItem

fun YearMonthItem.getYear(): Year {
    return when (this) {
        is YearItem -> year
        is MonthItem -> Year.of(yearMonth.year)
    }
}