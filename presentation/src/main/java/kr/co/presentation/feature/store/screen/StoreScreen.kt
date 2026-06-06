package kr.co.presentation.feature.store.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kr.co.presentation.theme.MinaryTheme
import kr.co.presentation.design.ThemePreviews


@Composable
fun StoreScreen() {
    StoreContent("Store")
}

@Composable
fun StoreContent(name: String) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Text(
            text = "Hello, $name!",
        )
    }
}

@ThemePreviews
@Composable
private fun StoreScreenPreview() {
    StoreContentPreview()
}

@Composable
fun StoreContentPreview() {
    MinaryTheme {
        StoreContent("Store Preview")
    }
}