package kr.co.presentation.feature.home.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kr.co.presentation.feature.calendar.navigation.calendarGraph
import kr.co.presentation.feature.dashboard.navigation.dashboardGraph
import kr.co.presentation.feature.search.navigation.searchGraph
import kr.co.presentation.feature.setting.navigation.settingsGraph
import kr.co.presentation.app.navigation.MinaryAppState

@Composable
fun HomeHost(
    appState: MinaryAppState,
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier,
) {
    NavHost(navController, startDestination, modifier) {
        calendarGraph(appState, navController)
        dashboardGraph(appState, navController)
        searchGraph(appState, navController)
        settingsGraph(appState, navController)
    }
}