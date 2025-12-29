package kr.co.presentation.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kr.co.presentation.ui.navigation.route.Setting
import kr.co.presentation.ui.navigation.route.SettingGraph
import kr.co.presentation.ui.screen.contents.setting.SettingScreen


internal fun NavGraphBuilder.settingNavGraph(
    navController: NavHostController
) {
    navigation<SettingGraph>(startDestination = Setting) {
        composable<Setting> {
            SettingScreen()
        }
    }
}