package kr.co.presentation.feature.calendar.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kr.co.domain.feature.diary.model.SyncStatus
import java.time.YearMonth

@Immutable
data class CalendarMonthItem(
    val yearMonth: YearMonth = YearMonth.now(),
    val days: ImmutableList<CalendarDayItem> = persistentListOf(),
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    override val key: String = "${yearMonth.year}-${yearMonth.monthValue}",
    override val contentType: CalendarBaseItem.ContentType = CalendarBaseItem.ContentType.TypeMonth,
) : CalendarGridItem