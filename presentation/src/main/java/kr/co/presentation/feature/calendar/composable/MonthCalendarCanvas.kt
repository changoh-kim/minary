package kr.co.presentation.feature.calendar.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.presentation.feature.calendar.extension.isToday
import kr.co.presentation.feature.calendar.model.CalendarMonthItem
import kr.co.presentation.feature.calendar.preview.model.CalendarMonthPreviewData
import kr.co.presentation.feature.calendar.preview.provider.CalendarMonthPreviewDataProvider
import kr.co.presentation.theme.MinaryTheme


@Composable
fun MonthCalendarCanvas(
    modifier: Modifier = Modifier,
    monthItem: CalendarMonthItem,
    onClick: () -> Unit = {},
) {
    val textMeasurer = rememberTextMeasurer()

    val titleStyle = remember {
        TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
        )
    }
    val sundayStyle = remember {
        TextStyle(fontSize = 10.sp, color = Color.Red, textAlign = TextAlign.Center)
    }
    val dayStyle = remember {
        TextStyle(fontSize = 10.sp, color = Color.Black, textAlign = TextAlign.Center)
    }
    val saturdayStyle = remember {
        TextStyle(fontSize = 10.sp, color = Color.Blue, textAlign = TextAlign.Center)
    }

    Canvas(
        modifier = modifier.then(
            Modifier
                .clickable { onClick() } // 월 전체 클릭 이벤트)
        )
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val cellWeight = canvasWidth / 7
        val paddingTop = 16.dp.toPx()

        val titleLayout = textMeasurer.measure(
            text = monthItem.yearMonth.monthValue.toString(),
            style = titleStyle
        )
        drawText(
            textLayoutResult = titleLayout,
            topLeft = Offset(
                x = (cellWeight - titleLayout.size.width) / 2,
                y = paddingTop
            )
        )

        val gridStartY = paddingTop + titleLayout.size.height

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
                    color = Color.Blue.copy(alpha = 0.3f),
                    radius = cellWeight / 2,
                    center = Offset(x = xPos + cellWeight / 2, y = yPos + cellWeight / 2)
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun MonthCalendarCanvasPreview(
    @PreviewParameter(CalendarMonthPreviewDataProvider::class) previewData: CalendarMonthPreviewData,
) {
    MinaryTheme {
        MonthCalendarCanvas(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(4.dp),
            monthItem = previewData.monthItem,
            onClick = {},
        )
    }
}