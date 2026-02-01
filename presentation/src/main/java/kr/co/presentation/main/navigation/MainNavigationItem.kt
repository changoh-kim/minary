package kr.co.presentation.main.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector


@Immutable
data class MainNavigationItem(
    @StringRes val labelResId: Int,
    val route: Any,
    val icon: ImageVector
)