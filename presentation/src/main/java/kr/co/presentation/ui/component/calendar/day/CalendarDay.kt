package kr.co.presentation.ui.component.calendar.day

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import kr.co.domain.model.calendar.date.CalendarDateData
import kr.co.domain.model.calendar.date.isAfterToday
import kr.co.domain.model.calendar.date.isToday
import kr.co.presentation.ui.navigation.route.Diary
import kr.co.presentation.ui.theme.MinaryTheme
import java.time.LocalDate


@Composable
fun CalendarDay(
    modifier: Modifier = Modifier,
    dateData: CalendarDateData,
    isIconVisible: Boolean = false,
    onClick: ((Diary) -> Unit)? = null
) {
    val isToday = dateData.isToday()

    val shape = if (isToday) CircleShape else MaterialTheme.shapes.small
    val color = if (isToday) Color.LightGray else Color.Transparent

    val textColor = if (isToday) Color.Red else MaterialTheme.colorScheme.onBackground
    val fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(
                enabled = (onClick != null),
                onClick = { onClick?.invoke(Diary(dateData.year, dateData.month, dateData.date)) }
            )
            .clip(shape)
            .background(color),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dateData.date.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = fontWeight,
        )

        if (isIconVisible) {
            dateData.icon?.let {
                if (!dateData.isAfterToday())
                    Text(text = it)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarDayPreview() {
    val today = LocalDate.now()
    MinaryTheme {
        CalendarDay(
            dateData = CalendarDateData(
                year = today.year,
                month = today.monthValue,
                date = today.dayOfMonth,
                icon = "\uD83D\uDE02"
            ),
            isIconVisible = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarDayPreview2() {
    val nextDay = LocalDate.now().plusDays(1)
    MinaryTheme {
        CalendarDay(
            dateData = CalendarDateData(
                year = nextDay.year,
                month = nextDay.monthValue,
                date = nextDay.dayOfMonth,
                icon = "\uD83D\uDE02"
            ),
            isIconVisible = false,
        )
    }
}