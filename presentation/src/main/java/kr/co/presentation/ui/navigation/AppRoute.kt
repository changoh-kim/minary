package kr.co.presentation.ui.navigation

import kotlinx.serialization.Serializable


sealed class AppRoute(
    open val route: String
) {
    object Auth : AppRoute("auth")
    object Welcome : AppRoute("${Auth.route}/welcome")
    object Login : AppRoute("${Auth.route}/login")
    object SignUp : AppRoute("${Auth.route}/signup")

    object Main : AppRoute("main")
    object MainContainer : AppRoute("${Main.route}/contents")

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
}