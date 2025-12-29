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
import kr.co.domain.model.calendar.MonthData
import kr.co.domain.model.calendar.date.BaseDateData
import kr.co.domain.model.calendar.date.CalendarDateData
import kr.co.domain.model.calendar.date.InactiveDateData
import kr.co.presentation.ui.component.calendar.day.CalendarDay
import kr.co.presentation.ui.component.calendar.day.OtherMonthDay
import kr.co.presentation.ui.theme.MinaryTheme
import java.time.YearMonth


@Composable
fun CalendarMonth(
    modifier: Modifier = Modifier,
    monthData: MonthData,
    onClick: ((MonthData) -> Unit)? = null,
    headerContent: @Composable ColumnScope.() -> Unit = {},
    dayContent: @Composable RowScope.(BaseDateData) -> Unit,
) {
    val weeks = remember(monthData) {
        monthData.days.chunked(7)
    }

    Column(
        modifier = modifier
            .clickable(
                enabled = onClick != null,
                onClick = { onClick?.invoke(monthData) }
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
                week.forEach { dateData ->
                    dayContent(dateData)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarMonthPreview() {
    MinaryTheme {
        val today = YearMonth.now()
        CalendarMonth(
            monthData = MonthData(
                year = today.year,
                month = today.monthValue,
            ),
        ) { dayData ->
            when (dayData) {
                is CalendarDateData -> CalendarDay(
                    modifier = Modifier.weight(1f),
                    dateData = dayData,
                    isIconVisible = true
                )

                is InactiveDateData -> OtherMonthDay(
                    modifier = Modifier.weight(1f),
                    date = dayData.date.toString()
                )
            }
        }
    }
}