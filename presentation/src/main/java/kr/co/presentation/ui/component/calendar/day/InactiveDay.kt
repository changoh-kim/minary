package kr.co.presentation.ui.component.calendar.day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.ui.model.calendar.day.InactiveDayItem
import kr.co.presentation.ui.theme.MinaryTheme


@Composable
fun InactiveDay(
    modifier: Modifier = Modifier,
    dayItem: InactiveDayItem,
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
    }
}

@Preview(showBackground = true)
@Composable
private fun InactiveDayPreview() {
    MinaryTheme {
        InactiveDay(dayItem = InactiveDayItem())
    }
}