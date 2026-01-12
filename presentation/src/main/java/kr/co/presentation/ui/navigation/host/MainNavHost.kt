package kr.co.presentation.ui.navigation.host

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kr.co.presentation.ui.navigation.graph.calenderNavGraph
import kr.co.presentation.ui.navigation.graph.dashBoardNavGraph
import kr.co.presentation.ui.navigation.graph.settingNavGraph
import kr.co.presentation.ui.navigation.graph.storeNavGraph
import kr.co.presentation.ui.navigation.route.AppRoute
import java.time.LocalDate


@Composable
fun MainNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: AppRoute,
    modifier: Modifier,
    onNavigateToDiaryScreen: (LocalDate) -> Unit,
) {
    NavHost(navController, startDestination, modifier) {
        calenderNavGraph(navController, onNavigateToDiaryScreen)
        dashBoardNavGraph(navController)
        storeNavGraph(navController)
        settingNavGraph(navController)
    }
}