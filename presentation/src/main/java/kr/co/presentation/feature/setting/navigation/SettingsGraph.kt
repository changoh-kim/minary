package kr.co.presentation.feature.setting.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.setting.screen.ProfileDetailScreen
import kr.co.presentation.feature.setting.screen.ProfileEditScreen
import kr.co.presentation.feature.setting.screen.SettingsScreen
import kr.co.presentation.navigation.MinaryAppState
import kr.co.presentation.navigation.ProfileDetailRoute
import kr.co.presentation.navigation.ProfileEditRoute
import kr.co.presentation.navigation.SettingsRoute


internal fun NavGraphBuilder.settingsGraph(
    appState: MinaryAppState,
    navController: NavHostController,
) {
    composable<SettingsRoute> {
        SettingsScreen(
            onUserProfileClicked = {
                navController.navigate(ProfileDetailRoute) {
                    launchSingleTop = true
                }
            },
            onSignOutSucceeded= { appState.navigateToWelcome() }
        )
    }

    composable<ProfileDetailRoute> {
        ProfileDetailScreen(
            onBackClicked = { navController.popBackStack() },
            onEditClicked = {
                navController.navigate(ProfileEditRoute) {
                    launchSingleTop = true
                }
            },
            onNavigateToAccountDeletion = { appState.navigateToAccountDeletion() }
        )
    }

    composable<ProfileEditRoute> {
        ProfileEditScreen(
            onBackClicked = { navController.popBackStack() },
            onProfileUpdateSucceeded = { navController.popBackStack() }
        )
    }
}