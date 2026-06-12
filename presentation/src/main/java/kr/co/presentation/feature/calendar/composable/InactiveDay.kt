package kr.co.presentation.feature.calendar.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.feature.calendar.model.CalendarDayItem
import kr.co.presentation.theme.MinaryTheme

@Composable
fun InactiveDay(
    modifier: Modifier = Modifier,
    dayItem: CalendarDayItem,
) {
    Column(
        modifier = modifier.aspectRatio(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${dayItem.date.dayOfMonth}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        Spacer(modifier = Modifier.height(6.dp))
    }
}

@ThemePreviews
@Composable
private fun InactiveDayPreview() {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            InactiveDay(
                modifier = Modifier.size(48.dp),
                dayItem = CalendarDayItem(isCurrentMonth = false)
            )
        }
    }
}