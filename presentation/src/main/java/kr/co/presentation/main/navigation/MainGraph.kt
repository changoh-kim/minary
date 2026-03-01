package kr.co.presentation.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kr.co.presentation.main.screen.MainScreen
import kr.co.presentation.navigation.MainRoute
import kr.co.presentation.navigation.MinaryAppState


internal fun NavGraphBuilder.mainGraph(
    appState: MinaryAppState,
) {
    composable<MainRoute> { MainScreen(appState) }
}