package kr.co.presentation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable


sealed class AppRoute(
    open val route: String
) {
    object Auth : AppRoute("auth")
    object Main : AppRoute("main")

    object Login : AppRoute("${Auth.route}/login")
    object Signup : AppRoute("${Auth.route}/signup")
    object MainContents : AppRoute("${Main.route}/contents")

    @Serializable
    data class Calender(val date: String = "")
    @Serializable
    data class Diary(val date: String = "")

    @Serializable
    object DashBoard
    @Serializable
    object Store
    @Serializable
    object Setting

    sealed class NavigationItem(
        val route: String,
        val icon: ImageVector,
        val name: String
    ) {
        object Calender : NavigationItem("calender", Icons.Filled.DateRange, "캘린더")
        object DashBoard : NavigationItem("dashboard", Icons.Filled.Star, "대시보드")
        object Store : NavigationItem("store", Icons.Filled.ShoppingCart, "상점")
        object Setting : NavigationItem("setting", Icons.Filled.Settings, "환경설정")
    }
}
