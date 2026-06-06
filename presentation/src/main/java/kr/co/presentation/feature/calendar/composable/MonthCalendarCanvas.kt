package kr.co.presentation.feature.calendar.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.presentation.feature.calendar.extension.isToday
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.preview.model.CalendarMonthPreviewData
import kr.co.presentation.feature.calendar.preview.provider.CalendarMonthPreviewDataProvider
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.theme.MinaryTheme
import kr.co.presentation.theme.Blue500


@Composable
fun MonthCalendarCanvas(
    modifier: Modifier = Modifier,
    monthItem: CalendarMonthItem,
    onClick: () -> Unit = {},
) {
    val textMeasurer = rememberTextMeasurer()

    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error

    val titleStyle = remember(onSurface) {
        TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onSurface,
            textAlign = TextAlign.Center,
        )
    }
    val sundayStyle = remember(error) {
        TextStyle(fontSize = 10.sp, color = error, textAlign = TextAlign.Center)
    }
    val dayStyle = remember(onSurface) {
        TextStyle(fontSize = 10.sp, color = onSurface, textAlign = TextAlign.Center)
    }
    val saturdayStyle = remember {
        TextStyle(fontSize = 10.sp, color = Blue500, textAlign = TextAlign.Center)
    }

    Canvas(
        modifier = modifier.then(
            Modifier
                .clickable { onClick() } // 월 전체 클릭 이벤트)
        )
    ) {
        val canvasWidth = size.width
        //val canvasHeight = size.height
        val cellWeight = canvasWidth / 7
        //val paddingTop = 16.dp.toPx()

        val titleLayout = textMeasurer.measure(
            text = monthItem.yearMonth.monthValue.toString(),
            style = titleStyle
        )
        drawText(
            textLayoutResult = titleLayout,
            topLeft = Offset(
                x = (cellWeight - titleLayout.size.width) / 2,
                y = 0f
            )
        )

        val gridStartY = titleLayout.size.height + 4.dp.toPx()

        monthItem.days.forEachIndexed { index, dayItem ->
            val row = index / 7
            val col = index % 7

            val xPos = col * cellWeight
            val yPos = gridStartY + (row * cellWeight)

            val currentStyle = when (col) {
                0 -> sundayStyle
                6 -> saturdayStyle
                else -> dayStyle
            }
            val dayText = when (dayItem.isCurrentMonth) {
                true -> dayItem.date.dayOfMonth.toString()
                false -> ""
            }

            val dayTextLayout = textMeasurer.measure(dayText, currentStyle)
            drawText(
                textLayoutResult = dayTextLayout,
                topLeft = Offset(
                    x = xPos + (cellWeight - dayTextLayout.size.width) / 2,
                    y = yPos + (cellWeight - dayTextLayout.size.height) / 2
                )
            )

            if (dayItem.isCurrentMonth && dayItem.isToday()) {
                drawCircle(
                    color = primary.copy(alpha = 0.3f),
                    radius = cellWeight / 2,
                    center = Offset(x = xPos + cellWeight / 2, y = yPos + cellWeight / 2)
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun MonthCalendarCanvasPreview(
    @PreviewParameter(CalendarMonthPreviewDataProvider::class) previewData: CalendarMonthPreviewData,
) {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MonthCalendarCanvas(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp),
                monthItem = previewData.monthItem,
                onClick = {},
            )
        }
    }
}