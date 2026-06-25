package kr.co.presentation.feature.dashboard.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.dashboard.screen.dashboard.DashboardScreen
import kr.co.presentation.app.navigation.MinaryAppState
import kr.co.presentation.app.navigation.route.DashboardRoute

internal fun NavGraphBuilder.dashboardGraph(
    appState: MinaryAppState,
    navController: NavHostController,
) {
    composable<DashboardRoute> { DashboardScreen() }
}