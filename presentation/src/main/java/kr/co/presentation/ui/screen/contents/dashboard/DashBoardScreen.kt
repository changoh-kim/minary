package kr.co.presentation.ui.screen.contents.dashboard

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.ui.theme.MinaryTheme


@Composable
fun DashBoardScreen() {
    DashBoardContents("DashBoard")
}

@Composable
fun DashBoardContents(name: String) {
    Text(
        text = "Hello $name!",
    )
}

@Preview(showBackground = true)
@Composable
fun DashBoardContentsPreview() {
    MinaryTheme {
        DashBoardContents("DashBoard")
    }
}