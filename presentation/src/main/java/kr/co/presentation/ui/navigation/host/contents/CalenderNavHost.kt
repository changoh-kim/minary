package kr.co.presentation.ui.navigation.host.contents

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kr.co.presentation.ui.navigation.AppRoute.Calender
import kr.co.presentation.ui.navigation.AppRoute.Diary
import kr.co.presentation.ui.screen.contents.CalendarScreen
import kr.co.presentation.ui.screen.diary.DiaryScreen

@SuppressLint("WrongStartDestinationType")
@Composable
fun CalenderNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Calender("2025-01-01")
    ) {
        composable<Calender> { backStackEntry ->
            val calender: Calender = backStackEntry.toRoute()
            CalendarScreen(
                onDateClick = { date -> navController.navigate(Diary(date)) },
            )
        }

        composable<Diary> { backStackEntry ->
            val diary: Diary = backStackEntry.toRoute()
            DiaryScreen(diary.date)
        }
    }
}

