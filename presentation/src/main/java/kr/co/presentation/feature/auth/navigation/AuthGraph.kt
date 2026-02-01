package kr.co.presentation.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.presentation.feature.auth.screen.LoginScreen
import kr.co.presentation.feature.auth.screen.SignUpScreen
import kr.co.presentation.feature.auth.screen.WelcomeScreen
import kr.co.presentation.main.navigation.MainRoute


internal fun NavGraphBuilder.authGraph(
    navController: NavHostController
) {

    composable<WelcomeRoute> {
        WelcomeScreen(
            onNavigateToLoginScreen = {
                navController.navigate(LoginRoute)
            }
        )
    }

    composable<LoginRoute> {
        LoginScreen(
            onNavigateToMainScreen = {
                navController.navigate(MainRoute) {
                    popUpTo(WelcomeRoute) {
                        inclusive = true
                    }
                }
            },
            onNavigateToSignUpScreen = {
                navController.navigate(SignUpRoute)
            }
        )
    }

    composable<SignUpRoute> {
        SignUpScreen(
            onNavigateToLoginScreen = {
                navController.popBackStack()
            }
        )
    }
}