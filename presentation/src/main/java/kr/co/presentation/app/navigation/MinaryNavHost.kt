package kr.co.presentation.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import kr.co.presentation.feature.account.navigation.accountGraph
import kr.co.presentation.feature.diary.navigation.diaryGraph
import kr.co.presentation.feature.home.navigation.homeGraph

@Composable
fun MinaryNavHost(
    appState: MinaryAppState,
    startDestination: Any,
) {
    NavHost(appState.navController, startDestination) {
        accountGraph(appState)
        homeGraph(appState)
        diaryGraph(appState)
    }
}