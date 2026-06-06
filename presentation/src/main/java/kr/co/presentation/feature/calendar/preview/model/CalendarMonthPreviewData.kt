package kr.co.presentation.feature.calendar.preview.model

import androidx.compose.runtime.Immutable
import kr.co.domain.feature.emotion.model.Emotion
import kr.co.presentation.feature.calendar.model.CalendarMonthItem


@Immutable
data class CalendarMonthPreviewData(
    val monthItem: CalendarMonthItem,
    val emotion: Emotion?
)