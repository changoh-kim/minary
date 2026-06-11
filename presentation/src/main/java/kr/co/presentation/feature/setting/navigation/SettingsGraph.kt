package kr.co.presentation.feature.setting.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kr.co.presentation.feature.setting.screen.ProfileDetailScreen
import kr.co.presentation.feature.setting.screen.ProfileEditScreen
import kr.co.presentation.feature.setting.screen.SettingsScreen
import kr.co.presentation.navigation.MinaryAppState
import kr.co.presentation.navigation.ProfileDetailRoute
import kr.co.presentation.navigation.ProfileEditRoute
import kr.co.presentation.navigation.SettingsRoute
import kr.co.presentation.navigation.SettingsTabRoute


internal fun NavGraphBuilder.settingsGraph(
    appState: MinaryAppState,
    navController: NavHostController,
) {
    navigation<SettingsTabRoute>(
        startDestination = SettingsRoute
    ) {
        composable<SettingsRoute> {
            SettingsScreen(
                onUserProfileClicked = {
                    navController.navigate(ProfileDetailRoute) {
                        launchSingleTop = true
                    }
                },
                onSignOutSucceeded = { appState.navigateToWelcome() }
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
}
