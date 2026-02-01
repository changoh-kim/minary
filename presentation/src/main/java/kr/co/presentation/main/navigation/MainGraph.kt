package kr.co.presentation.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.diary.navigation.DiaryRoute
import kr.co.presentation.main.screen.MainScreen


internal fun NavGraphBuilder.mainGraph(
    navController: NavHostController
) {

    composable<MainRoute> {
        MainScreen(
            onNavigateToDiaryScreen = { date ->
                navController.navigate(DiaryRoute(date.year, date.monthValue, date.dayOfMonth))
            },
        )
    }
}