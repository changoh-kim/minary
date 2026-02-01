package kr.co.presentation.feature.store.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.store.screen.StoreScreen


internal fun NavGraphBuilder.storeGraph(
    navController: NavHostController
) {

    composable<StoreRoute> { StoreScreen() }
}