package kr.co.presentation.feature.diary.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kr.co.presentation.feature.diary.screen.edit.DiaryEditScreen
import kr.co.presentation.feature.diary.screen.detail.DiaryDetailScreen
import kr.co.presentation.app.navigation.route.DiaryEditRoute
import kr.co.presentation.app.navigation.route.DiaryDetailRoute
import kr.co.presentation.app.navigation.MinaryAppState
import java.time.LocalDate


internal fun NavGraphBuilder.diaryGraph(
    appState: MinaryAppState,
) {
    composable<DiaryDetailRoute> {
        DiaryDetailScreen(
            onDiaryDeleted = { appState.navigateBack() },
            onLoadFailed = { appState.navigateBack() },
            onBack = { appState.navigateBack() },
            onNavigateToEdit = { date, isNewDiary ->
                if (isNewDiary) {
                    appState.navigateToDiaryEdit(date, isNewDiary = true) {
                        popUpTo<DiaryDetailRoute> { inclusive = true }
                    }
                } else {
                    appState.navigateToDiaryEdit(date, isNewDiary = false)
                }
            }
        )
    }

    composable<DiaryEditRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<DiaryEditRoute>()
        val date = LocalDate.of(route.year, route.month, route.date)

        DiaryEditScreen(
            onDiarySaved = {
                if (route.isNewDiary) {
                    appState.navigateToDiaryDetail(date) {
                        popUpTo<DiaryEditRoute> { inclusive = true }
                    }
                } else {
                    appState.navigateBack()
                }
            },
            onLoadFailed = { appState.navigateBack() },
            onBack = { appState.navigateBack() }
        )
    }
}
