package kr.co.presentation.feature.calendar.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kr.co.presentation.feature.calendar.model.CalendarGridItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.preview.provider.CalendarGridItemPreviewDataProvider
import kr.co.presentation.feature.calendar.preview.provider.CalendarMonthItemPreviewDataProvider
import kr.co.presentation.feature.calendar.screen.MonthlyCalendarPreviewContent
import kr.co.presentation.feature.calendar.screen.YearlyCalendarPreviewContent
import kr.co.presentation.design.MinaryPreviews


@MinaryPreviews
@Composable
private fun MonthlyCalendarScreenPreview(
    @PreviewParameter(CalendarMonthItemPreviewDataProvider::class)
    calendarMonths: Flow<PagingData<CalendarMonthItem>>
) {
    MonthlyCalendarPreviewContent(calendarMonths)
}

@MinaryPreviews
@Composable
private fun YearlyCalendarScreenPreview(
    @PreviewParameter(CalendarGridItemPreviewDataProvider::class)
    calendarGridItems: Flow<PagingData<CalendarGridItem>>
) {
    YearlyCalendarPreviewContent(calendarGridItems)
}