package kr.co.presentation.ui.component.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.ui.component.calendar.day.ActiveDay
import kr.co.presentation.ui.component.calendar.day.InactiveDay
import kr.co.presentation.ui.model.calendar.day.ActiveDayItem
import kr.co.presentation.ui.model.calendar.day.DayItem
import kr.co.presentation.ui.model.calendar.day.InactiveDayItem
import kr.co.presentation.ui.model.calendar.yearmonth.MonthItem
import kr.co.presentation.ui.preview.CalendarPreviewDataFactory
import kr.co.presentation.ui.theme.MinaryTheme
import java.time.YearMonth


@Composable
fun CalendarMonth(
    modifier: Modifier = Modifier,
    monthItem: MonthItem,
    onClick: ((YearMonth) -> Unit)? = null,
    headerContent: @Composable ColumnScope.() -> Unit = {},
    dayContent: @Composable RowScope.(DayItem) -> Unit,
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
        headerContent()

        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                week.forEach { dayItem ->
                    dayContent(dayItem)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarMonthPreview() {
    MinaryTheme {
        val monthItem = CalendarPreviewDataFactory.createMonthItem(YearMonth.now())
        CalendarMonth(
            monthItem = monthItem,
        ) { dayItem ->
            when (dayItem) {
                is ActiveDayItem -> ActiveDay(
                    modifier = Modifier.weight(1f),
                    dayItem = dayItem,
                    isIconVisible = true
                )

                is InactiveDayItem -> InactiveDay(
                    modifier = Modifier.weight(1f),
                    dayItem = dayItem
                )
            }
        }
    }
}