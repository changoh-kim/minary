package kr.co.presentation.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kr.co.presentation.feature.home.screen.HomeScreen
import kr.co.presentation.navigation.HomeRoute
import kr.co.presentation.navigation.MinaryAppState


internal fun NavGraphBuilder.homeGraph(
    appState: MinaryAppState,
) {
    composable<HomeRoute> { HomeScreen(appState = appState) }
}