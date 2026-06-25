package kr.co.presentation.feature.calendar.screen.monthly.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.feature.calendar.model.CalendarDayItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.preview.MonthCalendarPreviewData
import kr.co.presentation.feature.calendar.preview.MonthCalendarPreviewParameterProvider
import kr.co.presentation.feature.calendar.screen.monthly.component.preview.MonthCalendarPreviewDataFactory
import java.time.YearMonth

@Composable
fun MonthCalendar(
    modifier: Modifier = Modifier,
    monthItem: CalendarMonthItem,
    onClick: ((YearMonth) -> Unit)? = null,
    header: @Composable ColumnScope.() -> Unit = {},
    content: @Composable RowScope.(CalendarDayItem) -> Unit,
) {
    val weeks = remember(monthItem) {
        monthItem.days.chunked(7)
    }

    Column(
        modifier = modifier
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke(monthItem.yearMonth) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        header()

        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { dayItem ->
                    content(dayItem)
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun MonthCalendarPreview(
    @PreviewParameter(MonthCalendarPreviewParameterProvider::class)
    previewData: MonthCalendarPreviewData,
) {
    val diary = previewData.emotion?.let {
        MonthCalendarPreviewDataFactory.createDiary(it)
    }

    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MonthCalendar(
                monthItem = previewData.monthItem,
            ) { dayItem ->
                if (dayItem.isCurrentMonth)
                    ActiveDay(
                        modifier = Modifier.weight(1f),
                        dayItem = dayItem,
                        diary = diary,
                    )
                else
                    InactiveDay(modifier = Modifier.weight(1f), dayItem = dayItem)
            }
        }
    }
}
