package kr.co.presentation.feature.calendar.screen.monthly.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kr.co.domain.feature.calendar.generator.CalendarGenerator
import kr.co.presentation.feature.calendar.mapper.CalendarItemMapper.toCalendarMonthItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import java.time.YearMonth

internal class MonthlyCalendarScreenPreviewParameterProvider(
    calendarGenerator: CalendarGenerator = CalendarGenerator(),
) : PreviewParameterProvider<Flow<PagingData<CalendarMonthItem>>> {

    override val values: Sequence<Flow<PagingData<CalendarMonthItem>>> = sequenceOf(
        flowOf(
            PagingData.from(
                calendarGenerator.generateMonths(
                    YearMonth.now(),
                    12,
                    CalendarGenerator.SortOrder.Descending
                ).map { it.toCalendarMonthItem() }
            )
        ),
    )
}
