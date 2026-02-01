package kr.co.presentation.feature.calendar.preview.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.domain.feature.calendar.generator.CalendarGenerator
import kr.co.domain.feature.emotion.Emotion
import kr.co.presentation.feature.calendar.mapper.CalendarItemMapper.toCalendarMonthItem
import kr.co.presentation.feature.calendar.preview.model.CalendarMonthPreviewData
import java.time.YearMonth


internal class CalendarMonthPreviewDataProvider(
    calendarGenerator: CalendarGenerator = CalendarGenerator(),
) : PreviewParameterProvider<CalendarMonthPreviewData> {

    private val monthItem =
        calendarGenerator.generateMonth(YearMonth.now()).toCalendarMonthItem()

    override val values: Sequence<CalendarMonthPreviewData> = sequenceOf(
        CalendarMonthPreviewData(
            monthItem = monthItem,
            emotion = Emotion.SADNESS
        )
    )
}