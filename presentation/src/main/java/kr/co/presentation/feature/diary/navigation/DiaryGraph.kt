package kr.co.presentation.feature.diary.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.diary.screen.DiaryScreen


internal fun NavGraphBuilder.diaryGraph(
    navController: NavHostController
) {

    composable<DiaryRoute> {
        DiaryScreen(
            onNavigateToMainScreen = {
                navController.popBackStack()
            },
        )
    }
}