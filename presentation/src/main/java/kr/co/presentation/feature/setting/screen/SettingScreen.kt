package kr.co.presentation.feature.setting.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.theme.MinaryTheme


@Composable
fun SettingScreen() {
    SettingContent("Setting")
}

@Composable
fun SettingContent(name: String) {
    Text(
        text = "Hello $name!",
    )
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun SettingContentPreview() {
    MinaryTheme {
        SettingContent("Setting")
    }
}