package kr.co.presentation.feature.calendar.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.core.common.model.Emotion
import kr.co.domain.feature.calendar.generator.CalendarGenerator
import kr.co.presentation.feature.calendar.mapper.CalendarItemMapper.toCalendarMonthItem
import java.time.YearMonth

internal class MonthCalendarPreviewParameterProvider(
    calendarGenerator: CalendarGenerator = CalendarGenerator(),
) : PreviewParameterProvider<MonthCalendarPreviewData> {

    private val monthItem =
        calendarGenerator.generateMonth(YearMonth.now()).toCalendarMonthItem()

    override val values: Sequence<MonthCalendarPreviewData> = sequenceOf(
        MonthCalendarPreviewData(
            monthItem = monthItem,
            emotion = Emotion.SADNESS
        ),
    )
}