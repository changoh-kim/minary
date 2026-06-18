package kr.co.presentation.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.search.screen.SearchScreen
import kr.co.presentation.navigation.MinaryAppState
import kr.co.presentation.navigation.SearchRoute

internal fun NavGraphBuilder.searchGraph(
    appState: MinaryAppState,
    navController: NavHostController,
) {
    composable<SearchRoute> {
        SearchScreen(
            onDiaryClicked = { date -> appState.navigateToDiaryPreview(date) }
        )
    }
}