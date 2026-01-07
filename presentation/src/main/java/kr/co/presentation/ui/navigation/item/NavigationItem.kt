package kr.co.presentation.ui.navigation.item

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector


@Immutable
data class NavigationItem<T : Any>(
    val labelResId: Int,
    val route: T,
    val icon: ImageVector
)