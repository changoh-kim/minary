package kr.co.presentation.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kr.co.presentation.feature.calendar.navigation.calenderGraph
import kr.co.presentation.feature.dashboard.navigation.dashboardGraph
import kr.co.presentation.feature.setting.navigation.settingGraph
import kr.co.presentation.feature.store.navigation.storeGraph
import kr.co.presentation.main.viewmodel.MainIntent


@Composable
fun MainHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Any,
    modifier: Modifier,
    intent: (MainIntent) -> Unit = {},
) {

    NavHost(navController, startDestination, modifier) {
        calenderGraph(navController, intent)
        dashboardGraph(navController)
        storeGraph(navController)
        settingGraph(navController)
    }
}