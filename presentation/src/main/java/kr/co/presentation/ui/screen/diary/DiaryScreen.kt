package kr.co.presentation.ui.screen.diary

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.ui.theme.MinaryTheme
import java.time.LocalDate


@Composable
fun DiaryScreen(
    date: LocalDate = LocalDate.now(),
    onNavigateToMainScreen : () -> Unit = {}
) {
    Text(
        text = "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일",
    )
}

@Preview(showBackground = true)
@Composable
private fun DiaryScreenPreview() {
    MinaryTheme {
        DiaryScreen()
    }
}