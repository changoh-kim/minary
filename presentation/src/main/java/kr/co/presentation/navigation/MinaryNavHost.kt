package kr.co.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import kr.co.presentation.feature.auth.navigation.authGraph
import kr.co.presentation.feature.diary.navigation.diaryGraph
import kr.co.presentation.main.navigation.mainGraph


@Composable
fun MinaryNavHost(
    appState: MinaryAppState,
    startDestination: Any,
) {
    NavHost(appState.navController, startDestination) {
        authGraph(appState)
        mainGraph(appState)
        diaryGraph(appState)
    }
}