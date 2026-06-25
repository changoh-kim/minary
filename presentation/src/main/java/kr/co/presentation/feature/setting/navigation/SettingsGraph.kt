package kr.co.presentation.feature.setting.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kr.co.presentation.feature.setting.screen.profiledetail.ProfileDetailScreen
import kr.co.presentation.feature.setting.screen.profileedit.ProfileEditScreen
import kr.co.presentation.feature.setting.screen.settings.SettingsScreen
import kr.co.presentation.app.navigation.MinaryAppState
import kr.co.presentation.app.navigation.route.ProfileDetailRoute
import kr.co.presentation.app.navigation.route.ProfileEditRoute
import kr.co.presentation.app.navigation.route.SettingsRoute
import kr.co.presentation.app.navigation.route.SettingsTabRoute

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
