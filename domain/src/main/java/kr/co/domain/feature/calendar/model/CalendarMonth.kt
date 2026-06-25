package kr.co.domain.feature.calendar.model

import kr.co.core.common.state.SyncStatus
import java.time.YearMonth


data class CalendarMonth(
    val yearMonth: YearMonth,
    val days: List<CalendarDay>,
    val syncStatus: SyncStatus = SyncStatus.IDLE
)