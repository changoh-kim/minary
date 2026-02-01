package kr.co.presentation.navigation.host

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kr.co.presentation.feature.auth.navigation.authGraph
import kr.co.presentation.feature.diary.navigation.diaryGraph
import kr.co.presentation.main.navigation.mainGraph


@Composable
fun AppNaveGraph(
    navController: NavHostController,
    startDestination: Any,
) {

    NavHost(navController, startDestination) {
        authGraph(navController)
        mainGraph(navController)
        diaryGraph(navController)
    }
}