package kr.co.presentation.ui.screen.contents.store

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.ui.theme.MinaryTheme


@Composable
fun StoreScreen() {
    StoreContents("Store")
}

@Composable
fun StoreContents(name: String) {
    Text(
        text = "Hello $name!",
    )
}

@Preview(showBackground = true)
@Composable
fun StoreContentsPreview() {
    MinaryTheme {
        StoreContents("Store")
    }
}