package kr.co.presentation.feature.calendar.model

import androidx.compose.runtime.Immutable
import java.time.Year

@Immutable
data class CalendarYearItem(
    val year: Year = Year.now(),
    override val key: String = year.toString(),
    override val contentType: CalendarBaseItem.ContentType = CalendarBaseItem.ContentType.TypeYear,
) : CalendarGridItem