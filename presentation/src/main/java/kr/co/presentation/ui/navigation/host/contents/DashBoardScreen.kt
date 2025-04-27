package kr.co.presentation.ui.navigation.host.contents

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kr.co.presentation.ui.navigation.AppRoute.DashBoard
import kr.co.presentation.ui.screen.contents.DashBoardScreen

@SuppressLint("WrongStartDestinationType")
@Composable
fun DashBoardNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = DashBoard
    ) {
        composable<DashBoard> {
            DashBoardScreen()
        }
    }
}