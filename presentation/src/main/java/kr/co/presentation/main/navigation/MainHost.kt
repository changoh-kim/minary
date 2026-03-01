package kr.co.presentation.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kr.co.presentation.feature.calendar.navigation.calenderGraph
import kr.co.presentation.feature.dashboard.navigation.dashboardGraph
import kr.co.presentation.feature.setting.navigation.settingGraph
import kr.co.presentation.feature.store.navigation.storeGraph
import kr.co.presentation.navigation.MinaryAppState


@Composable
fun MainHost(
    appState: MinaryAppState,
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier,
) {
    NavHost(navController, startDestination, modifier) {
        calenderGraph(appState, navController)
        dashboardGraph(appState, navController)
        storeGraph(appState, navController)
        settingGraph(appState, navController)
    }
}