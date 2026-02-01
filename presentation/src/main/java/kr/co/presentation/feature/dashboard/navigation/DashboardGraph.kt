package kr.co.presentation.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.dashboard.screen.DashboardScreen


internal fun NavGraphBuilder.dashboardGraph(
    navController: NavHostController
) {

    composable<DashboardRoute> { DashboardScreen() }
}