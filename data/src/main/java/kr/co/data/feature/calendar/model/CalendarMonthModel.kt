package kr.co.data.feature.calendar.model

import kr.co.domain.feature.diary.model.SyncStatus
import java.time.YearMonth

data class CalendarMonthModel(
    val yearMonth: YearMonth,
    val days: List<CalendarDayModel>,
    val syncStatus: SyncStatus = SyncStatus.IDLE
)