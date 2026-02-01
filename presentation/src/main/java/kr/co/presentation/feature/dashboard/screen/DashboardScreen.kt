package kr.co.presentation.feature.dashboard.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.theme.MinaryTheme


@Composable
fun DashboardScreen() {
    DashboardContent("Dashboard")
}

@Composable
fun DashboardContent(name: String) {
    Text(
        text = "Hello $name!",
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardContentPreview() {
    MinaryTheme {
        DashboardContent("Dashboard")
    }
}