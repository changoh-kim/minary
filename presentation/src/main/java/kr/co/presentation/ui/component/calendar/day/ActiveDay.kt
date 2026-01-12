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
import kr.co.presentation.ui.extension.isAfterToday
import kr.co.presentation.ui.extension.isToday
import kr.co.presentation.ui.model.calendar.day.ActiveDayItem
import kr.co.presentation.ui.theme.MinaryTheme
import java.time.LocalDate


@Composable
fun ActiveDay(
    modifier: Modifier = Modifier,
    dayItem: ActiveDayItem,
    isIconVisible: Boolean = false,
    onClick: ((LocalDate) -> Unit)? = null
) {
    val isToday = dayItem.isToday()

    val shape = if (isToday) CircleShape else MaterialTheme.shapes.small
    val color = if (isToday) Color.LightGray else Color.Transparent

    val textColor = if (isToday) Color.Red else MaterialTheme.colorScheme.onBackground
    val fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(
                enabled = (onClick != null),
                onClick = { onClick?.invoke(dayItem.date) }
            )
            .clip(shape)
            .background(color),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${dayItem.date.dayOfMonth}",
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = fontWeight,
        )

        if (isIconVisible) {
            dayItem.icon?.let {
                if (!dayItem.isAfterToday())
                    Text(text = it)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ActiveDayPreview() {
    MinaryTheme {
        ActiveDay(
            dayItem = ActiveDayItem(),
            isIconVisible = true,
        )
    }
}