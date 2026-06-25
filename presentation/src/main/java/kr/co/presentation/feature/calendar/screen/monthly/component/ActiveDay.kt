package kr.co.presentation.feature.calendar.screen.monthly.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.presentation.feature.calendar.extension.isToday
import kr.co.presentation.feature.calendar.model.CalendarDayItem
import kr.co.presentation.feature.calendar.model.CalendarDiaryUiModel
import java.time.LocalDate

@Composable
fun ActiveDay(
    modifier: Modifier = Modifier,
    dayItem: CalendarDayItem,
    diary: CalendarDiaryUiModel? = null,
    onClick: ((CalendarDayItem) -> Unit)? = null
) {
    val isToday = dayItem.isToday()

    val backgroundColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    val textColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(
                enabled = (onClick != null),
                onClick = { onClick?.invoke(dayItem) }
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${dayItem.date.dayOfMonth}",
            fontSize = 14.sp,
            color = textColor,
            fontWeight = fontWeight,
        )

        if (diary != null) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .background(if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@ThemePreviews
@Composable
private fun ActiveDayTodayPreview() {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ActiveDay(
                modifier = Modifier.size(48.dp),
                dayItem = CalendarDayItem(
                    isCurrentMonth = true,
                ),
                diary = CalendarDiaryUiModel(),
            )
        }
    }
}

@ThemePreviews
@Composable
private fun ActiveDayPreview() {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val yesterday = LocalDate.now().minusDays(1)
            ActiveDay(
                modifier = Modifier.size(48.dp),
                dayItem = CalendarDayItem(
                    isCurrentMonth = true,
                    date = yesterday
                ),
                diary = CalendarDiaryUiModel(date = yesterday),
            )
        }
    }
}
