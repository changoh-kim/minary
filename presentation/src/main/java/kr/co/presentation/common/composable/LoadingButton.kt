package kr.co.presentation.common.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.presentation.theme.MinaryTheme


@Composable
fun LoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors()
    ) {
        Box {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            }

            Text(
                text = text,
                color = if (isLoading) Color.Transparent else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(name = "LoadingButton", showBackground = true)
@Composable
private fun LoadingButtonNotLoadingPreview() {
    MinaryTheme {
        LoadingButton(
            text = "Loading Button",
            onClick = { },
            isLoading = false,
        )
    }
}

@Preview(name = "LoadingButton - Loading", showBackground = true)
@Composable
private fun LoadingButtonLoadingPreview() {
    MinaryTheme {
        LoadingButton(
            text = "Loading Button",
            onClick = { },
            isLoading = true,
        )
    }
}