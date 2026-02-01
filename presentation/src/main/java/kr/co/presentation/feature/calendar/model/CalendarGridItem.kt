package kr.co.presentation.feature.calendar.model

import androidx.compose.runtime.Immutable
import java.time.Year


@Immutable
sealed interface CalendarGridItem : CalendarBaseItem

val CalendarGridItem.year: Year
    get() = when (this) {
        is CalendarYearItem -> year
        is CalendarMonthItem -> Year.of(yearMonth.year)
    }