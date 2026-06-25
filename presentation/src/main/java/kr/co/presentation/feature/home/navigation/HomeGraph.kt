package kr.co.presentation.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kr.co.presentation.feature.home.screen.home.HomeScreen
import kr.co.presentation.app.navigation.MinaryAppState
import kr.co.presentation.app.navigation.route.HomeRoute

internal fun NavGraphBuilder.homeGraph(
    appState: MinaryAppState,
) {
    composable<HomeRoute> { HomeScreen(appState = appState) }
}