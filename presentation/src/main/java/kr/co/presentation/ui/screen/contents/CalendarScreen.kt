package kr.co.presentation.ui.screen.contents

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.ui.theme.MinaryTheme

@Composable
fun CalendarScreen(onDateClick: (String) -> Unit = {}) {
    CalendarContents("Calender")
}

@Composable
private fun CalendarContents(name: String) {
    Text(
        text = "Hello $name!",
    )
}

@Preview(showBackground = true)
@Composable
private fun CalendarContentsPreview() {
    MinaryTheme {
        CalendarContents("Calender")
    }
}
