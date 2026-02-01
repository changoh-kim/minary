package kr.co.presentation.feature.calendar.composable

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
import androidx.compose.ui.tooling.preview.PreviewParameter
import kr.co.presentation.feature.calendar.model.CalendarDayItem
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.preview.model.CalendarMonthPreviewData
import kr.co.presentation.feature.calendar.preview.provider.CalendarMonthPreviewDataProvider
import kr.co.presentation.feature.diary.preview.factory.DiaryPreviewDataFactory
import kr.co.presentation.theme.MinaryTheme
import java.time.YearMonth


@Composable
fun CalendarMonth(
    modifier: Modifier = Modifier,
    monthItem: CalendarMonthItem,
    onClick: ((YearMonth) -> Unit)? = null,
    headerContent: @Composable ColumnScope.() -> Unit = {},
    dayContent: @Composable RowScope.(CalendarDayItem) -> Unit,
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

@Preview(showBackground = true, locale = "ko")
@Composable
private fun CalendarMonthPreview(
    @PreviewParameter(CalendarMonthPreviewDataProvider::class) previewData: CalendarMonthPreviewData,
) {
    val diary = previewData.emotion?.let {
        DiaryPreviewDataFactory.createDiary(it)
    }

    MinaryTheme {
        CalendarMonth(
            monthItem = previewData.monthItem,
        ) { dayItem ->
            when (dayItem.isCurrentMonth) {
                true -> ActiveDay(
                    modifier = Modifier.weight(1f),
                    dayItem = dayItem,
                    diary = diary,
                    isIconVisible = true
                )

                false -> InactiveDay(
                    modifier = Modifier.weight(1f),
                    dayItem = dayItem
                )
            }
        }
    }
}