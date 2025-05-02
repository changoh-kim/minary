package kr.co.presentation.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kr.co.presentation.ui.navigation.AppRoute.Auth
import kr.co.presentation.ui.navigation.AppRoute.Login
import kr.co.presentation.ui.navigation.AppRoute.MainContainer
import kr.co.presentation.ui.navigation.AppRoute.SignUp
import kr.co.presentation.ui.navigation.AppRoute.Welcome
import kr.co.presentation.ui.screen.login.LoginScreen
import kr.co.presentation.ui.screen.login.SignUpScreen
import kr.co.presentation.ui.screen.login.WelcomeScreen


internal fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        route = Auth.route,
        startDestination = Welcome.route
    ) {
        composable(Welcome.route) {
            WelcomeScreen(
                onNavigateToLoginScreen = {
                    navController.navigate(Login.route)
                }
            )
        }

        composable(Login.route) {
            LoginScreen(
                onNavigateToMainScreen = {
                    navController.navigate(MainContainer.route) {
                        popUpTo(Welcome.route) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToSignupScreen = {
                    navController.navigate(SignUp.route)
                }
            )
        }

        composable(SignUp.route) {
            SignUpScreen(
                onNavigateToLoginScreen = {
                    navController.popBackStack()
                }
            )
        }
    }
}