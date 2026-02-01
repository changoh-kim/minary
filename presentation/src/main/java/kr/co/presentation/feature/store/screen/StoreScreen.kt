package kr.co.presentation.feature.store.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.theme.MinaryTheme


@Composable
fun StoreScreen() {
    StoreContent("Store")
}

@Composable
fun StoreContent(name: String) {
    Text(
        text = "Hello $name!",
    )
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun StoreContentPreview() {
    MinaryTheme {
        StoreContent("Store")
    }
}