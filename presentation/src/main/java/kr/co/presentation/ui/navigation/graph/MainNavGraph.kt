package kr.co.presentation.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kr.co.presentation.ui.navigation.route.Diary
import kr.co.presentation.ui.navigation.route.Main
import kr.co.presentation.ui.navigation.route.MainGraph
import kr.co.presentation.ui.screen.diary.DiaryScreen
import kr.co.presentation.ui.screen.main.MainScreen


internal fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController
) {
    navigation<MainGraph>(startDestination = Main) {
        composable<Main> {
            MainScreen(
                onNavigateToDiaryScreen = { diary ->
                    navController.navigate(diary)
                }
            )
        }
        composable<Diary> { backStackEntry ->
            val diary: Diary = backStackEntry.toRoute()
            DiaryScreen(
                year = diary.year,
                month = diary.month,
                date = diary.date,
                onNavigateToMainScreen = {
                    navController.popBackStack()
                }
            )
        }
    }
}