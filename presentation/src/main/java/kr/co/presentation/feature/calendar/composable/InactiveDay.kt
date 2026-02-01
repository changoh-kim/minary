package kr.co.presentation.feature.calendar.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.presentation.feature.calendar.model.CalendarDayItem
import kr.co.presentation.theme.MinaryTheme


@Composable
fun InactiveDay(
    modifier: Modifier = Modifier,
    dayItem: CalendarDayItem,
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.small)
            .background(color = Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${dayItem.date.dayOfMonth}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray,
            fontWeight = FontWeight.Normal,
        )

        Spacer(modifier = Modifier.padding(6.dp).size(6.dp))
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun InactiveDayPreview() {
    MinaryTheme {
        InactiveDay(dayItem = CalendarDayItem(isCurrentMonth = false))
    }
}