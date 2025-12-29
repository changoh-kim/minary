package kr.co.presentation.ui.screen.diary

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.ui.theme.MinaryTheme
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth


@Composable
fun DiaryScreen(
    year: Int = Year.now().value,
    month: Int = YearMonth.now().monthValue,
    date: Int = LocalDate.now().dayOfMonth,
    onNavigateToMainScreen : () -> Unit = {}
) {

    Text(
        text = "${year}년 ${month}월 ${date}일",
    )
}

@Preview(showBackground = true)
@Composable
private fun DiaryScreenPreview() {
    MinaryTheme {
        DiaryScreen()
    }
}