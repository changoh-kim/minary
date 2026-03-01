package kr.co.presentation.feature.setting.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.setting.screen.SettingScreen
import kr.co.presentation.navigation.MinaryAppState
import kr.co.presentation.navigation.SettingRoute


internal fun NavGraphBuilder.settingGraph(
    appState: MinaryAppState,
    navController: NavHostController,
) {
    composable<SettingRoute> { SettingScreen() }
}