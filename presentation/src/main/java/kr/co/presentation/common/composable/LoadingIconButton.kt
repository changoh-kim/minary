package kr.co.presentation.common.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.theme.MinaryTheme


@Composable
fun LoadingIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    iconContent: @Composable (Boolean) -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        colors = IconButtonDefaults.iconButtonColors(),
    ) {
        Box {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp
                )
            }
            iconContent(isLoading)
        }
    }
}

@ThemePreviews
@Composable
private fun LoadingIconButtonNotLoadingPreview() {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoadingIconButton(
                onClick = { },
                isLoading = false,
            ) { isLoading ->
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add",
                    tint = if (isLoading) Color.Transparent else LocalContentColor.current,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun LoadingIconButtonLoadingPreview() {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoadingIconButton(
                onClick = { },
                isLoading = true,
            ) { isLoading ->
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add",
                    tint = if (isLoading) Color.Transparent else LocalContentColor.current,
                )
            }
        }
    }
}