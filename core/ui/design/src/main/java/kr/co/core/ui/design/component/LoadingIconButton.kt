package kr.co.core.ui.design.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme

@Composable
fun LoadingIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    iconContent: @Composable (Boolean) -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        colors = colors,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colors.contentColor,
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
                isLoading = false,
                onClick = { },
                modifier = Modifier.padding(end = 4.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                iconContent = { isLoading ->
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "check",
                        tint = if (isLoading) Color.Transparent else MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
        }
    }
}

@ThemePreviews
@Composable
private fun LoadingIconButtonLoadingPreview() {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoadingIconButton(
                isLoading = true,
                onClick = { },
                modifier = Modifier.padding(end = 4.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                iconContent = { isLoading ->
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "check",
                        tint = if (isLoading) Color.Transparent else MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
        }
    }
}
