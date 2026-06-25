package kr.co.presentation.feature.calendar.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class CalendarDayItem(
    val date: LocalDate = LocalDate.now(),
    val isCurrentMonth: Boolean = true,
    val diary: CalendarDiaryUiModel? = null,
    override val key: String = "${date.year}-${date.monthValue}-${date.dayOfMonth}",
    override val contentType: CalendarBaseItem.ContentType = CalendarBaseItem.ContentType.TypeDay,
) : CalendarBaseItem
