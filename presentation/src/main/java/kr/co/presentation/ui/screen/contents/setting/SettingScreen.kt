package kr.co.presentation.ui.screen.contents.setting

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.ui.theme.MinaryTheme


@Composable
fun SettingScreen() {
    SettingContents("Setting")
}

@Composable
fun SettingContents(name: String) {
    Text(
        text = "Hello $name!",
    )
}

@Preview(showBackground = true)
@Composable
fun SettingContentsPreview() {
    MinaryTheme {
        SettingContents("Setting")
    }
}