package kr.co.presentation.ui.screen.diary

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.ui.theme.MinaryTheme

@Composable
fun DiaryScreen(date: String) {
    DiaryScreen()
}

@Composable
fun DiaryScreen() {
    Text(
        text = "Hello Diary Screen!",
    )
}

@Preview(showBackground = true)
@Composable
fun DiaryScreenPreview() {
    MinaryTheme {
        DiaryScreen()
    }
}