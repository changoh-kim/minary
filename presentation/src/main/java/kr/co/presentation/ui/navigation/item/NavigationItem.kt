package kr.co.presentation.ui.navigation.item

import androidx.compose.ui.graphics.vector.ImageVector


data class NavigationItem<T : Any>(val label: String, val route: T, val icon: ImageVector)
