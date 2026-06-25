package kr.co.presentation.feature.home.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class HomeNavigationItem(
    @StringRes val labelResId: Int,
    val route: Any,
    val icon: ImageVector
)