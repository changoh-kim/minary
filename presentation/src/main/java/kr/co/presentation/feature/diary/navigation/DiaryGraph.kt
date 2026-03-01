package kr.co.presentation.feature.diary.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kr.co.presentation.feature.diary.screen.DiaryScreen
import kr.co.presentation.navigation.DiaryRoute
import kr.co.presentation.navigation.MinaryAppState


internal fun NavGraphBuilder.diaryGraph(
    appState: MinaryAppState,
) {
    composable<DiaryRoute> {
        DiaryScreen(
            onDiaryDeleted = { appState.navigateBack() },
            onLoadFailed = { appState.navigateBack() }
        )
    }
}