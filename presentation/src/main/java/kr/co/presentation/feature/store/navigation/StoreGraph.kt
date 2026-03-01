package kr.co.presentation.feature.store.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.store.screen.StoreScreen
import kr.co.presentation.navigation.MinaryAppState
import kr.co.presentation.navigation.StoreRoute


internal fun NavGraphBuilder.storeGraph(
    appState: MinaryAppState,
    navController: NavHostController,
) {
    composable<StoreRoute> { StoreScreen() }
}