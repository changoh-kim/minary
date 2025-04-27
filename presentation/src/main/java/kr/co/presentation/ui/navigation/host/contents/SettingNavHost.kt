package kr.co.presentation.ui.navigation.host.contents

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kr.co.presentation.ui.navigation.AppRoute.Setting
import kr.co.presentation.ui.screen.contents.SettingScreen

@SuppressLint("WrongStartDestinationType")
@Composable
fun SettingNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Setting
    ) {
        composable<Setting> {
            SettingScreen()
        }
    }
}