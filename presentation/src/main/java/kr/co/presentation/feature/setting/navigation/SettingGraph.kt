package kr.co.presentation.feature.setting.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.setting.screen.SettingScreen


internal fun NavGraphBuilder.settingGraph(
    navController: NavHostController
) {

    composable<SettingRoute> { SettingScreen() }
}