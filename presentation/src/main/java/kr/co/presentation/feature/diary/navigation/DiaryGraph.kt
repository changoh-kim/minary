package kr.co.presentation.feature.diary.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kr.co.presentation.feature.diary.screen.DiaryEditScreen
import kr.co.presentation.feature.diary.screen.DiaryPreviewScreen
import kr.co.presentation.navigation.DiaryEditRoute
import kr.co.presentation.navigation.DiaryPreviewRoute
import kr.co.presentation.navigation.MinaryAppState
import java.time.LocalDate


internal fun NavGraphBuilder.diaryGraph(
    appState: MinaryAppState,
) {
    composable<DiaryPreviewRoute> {
        DiaryPreviewScreen(
            onDiaryDeleted = { appState.navigateBack() },
            onLoadFailed = { appState.navigateBack() },
            onNavigateToEdit = { date, isNewDiary ->
                if (isNewDiary) {
                    appState.navigateToDiaryEdit(date, isNewDiary = true) {
                        popUpTo<DiaryPreviewRoute> { inclusive = true }
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
                    appState.navigateToDiaryPreview(date) {
                        popUpTo<DiaryEditRoute> { inclusive = true }
                    }
                } else {
                    appState.navigateBack()
                }
            },
            onLoadFailed = { appState.navigateBack() }
        )
    }
}
