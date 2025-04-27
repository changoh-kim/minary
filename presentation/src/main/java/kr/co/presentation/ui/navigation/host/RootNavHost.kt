package kr.co.presentation.ui.navigation.host

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kr.co.presentation.ui.navigation.graph.authNavGraph
import kr.co.presentation.ui.navigation.graph.mainNavGraph

@Composable
fun RootNaveGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController, startDestination) {
        authNavGraph(navController)
        mainNavGraph(navController)
    }
}