package kr.co.presentation.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kr.co.presentation.ui.navigation.route.AuthGraph
import kr.co.presentation.ui.navigation.route.Login
import kr.co.presentation.ui.navigation.route.MainGraph
import kr.co.presentation.ui.navigation.route.SignUp
import kr.co.presentation.ui.navigation.route.Welcome
import kr.co.presentation.ui.screen.auth.LoginScreen
import kr.co.presentation.ui.screen.auth.SignUpScreen
import kr.co.presentation.ui.screen.auth.WelcomeScreen


internal fun NavGraphBuilder.authNavGraph(
    navController: NavHostController
) {
    navigation<AuthGraph>(startDestination = Welcome) {
        composable<Welcome> {
            WelcomeScreen(
                onNavigateToLoginScreen = {
                    navController.navigate(Login)
                }
            )
        }
        composable<Login> {
            LoginScreen(
                onNavigateToMainScreen = {
                    navController.navigate(MainGraph) {
                        popUpTo(Welcome) {
                            inclusive = true
                        }
                    }
                },
                onNavigateToSignupScreen = {
                    navController.navigate(SignUp)
                }
            )
        }
        composable<SignUp> {
            SignUpScreen(
                onNavigateToLoginScreen = {
                    navController.popBackStack()
                }
            )
        }
    }
}