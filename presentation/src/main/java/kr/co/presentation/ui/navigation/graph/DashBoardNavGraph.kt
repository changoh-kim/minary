package kr.co.presentation.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kr.co.presentation.ui.navigation.route.DashBoard
import kr.co.presentation.ui.navigation.route.DashBoardGraph
import kr.co.presentation.ui.screen.contents.dashboard.DashBoardScreen


internal fun NavGraphBuilder.dashBoardNavGraph(
    navController: NavHostController
) {
    navigation<DashBoardGraph>(startDestination = DashBoard) {
        composable<DashBoard> {
            DashBoardScreen()
        }
    }
}