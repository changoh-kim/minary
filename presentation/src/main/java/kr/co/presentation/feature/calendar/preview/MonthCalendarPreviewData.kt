package kr.co.presentation.feature.calendar.preview

import androidx.compose.runtime.Immutable
import kr.co.core.common.model.Emotion
import kr.co.presentation.feature.calendar.model.CalendarMonthItem

@Immutable
data class MonthCalendarPreviewData(
    val monthItem: CalendarMonthItem,
    val emotion: Emotion?
)