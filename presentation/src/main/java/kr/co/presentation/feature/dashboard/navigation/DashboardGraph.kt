package kr.co.presentation.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.dashboard.screen.DashboardScreen
import kr.co.presentation.navigation.DashboardRoute
import kr.co.presentation.navigation.MinaryAppState


internal fun NavGraphBuilder.dashboardGraph(
    appState: MinaryAppState,
    navController: NavHostController,
) {
    composable<DashboardRoute> { DashboardScreen() }
}