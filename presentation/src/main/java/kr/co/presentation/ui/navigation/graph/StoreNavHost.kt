package kr.co.presentation.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kr.co.presentation.ui.navigation.route.Store
import kr.co.presentation.ui.navigation.route.StoreGraph
import kr.co.presentation.ui.screen.contents.store.StoreScreen

internal fun NavGraphBuilder.storeNavGraph(
    navController: NavHostController
) {
    navigation<StoreGraph>(startDestination = Store) {
        composable<Store> {
            StoreScreen()
        }
    }
}