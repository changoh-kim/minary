package kr.co.presentation.feature.calendar.screen.yearly.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kr.co.domain.feature.calendar.generator.CalendarGenerator
import kr.co.presentation.feature.calendar.mapper.CalendarItemMapper.toCalendarMonthItem
import kr.co.presentation.feature.calendar.mapper.insertYearSeparators
import kr.co.presentation.feature.calendar.model.CalendarGridItem
import java.time.Year

internal class YearlyCalendarScreenPreviewParameterProvider(
    calendarGenerator: CalendarGenerator = CalendarGenerator(),
) :
    PreviewParameterProvider<Flow<PagingData<CalendarGridItem>>> {

    override val values: Sequence<Flow<PagingData<CalendarGridItem>>> = sequenceOf(
        flowOf(
            PagingData.from(
                calendarGenerator.generateMonths(Year.now()).map {
                    it.toCalendarMonthItem()
                }
            ).insertYearSeparators()
        ),
    )
}