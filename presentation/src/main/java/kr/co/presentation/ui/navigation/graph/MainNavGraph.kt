package kr.co.presentation.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kr.co.presentation.ui.navigation.AppRoute.Main
import kr.co.presentation.ui.navigation.AppRoute.MainContents
import kr.co.presentation.ui.screen.MainScreen

internal fun NavGraphBuilder.mainNavGraph(navController: NavHostController) {
    navigation(
        route = Main.route,
        startDestination = MainContents.route
    ) {
        composable(MainContents.route) {
            MainScreen()
        }
    }
}