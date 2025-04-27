package kr.co.presentation.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kr.co.presentation.ui.navigation.AppRoute.Auth
import kr.co.presentation.ui.navigation.AppRoute.Login
import kr.co.presentation.ui.navigation.AppRoute.Signup
import kr.co.presentation.ui.screen.login.LoginScreen
import kr.co.presentation.ui.screen.login.SignupScreen

internal fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        route = Auth.route,
        startDestination = Login.route
    ) {
        composable(Login.route) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Signup) {}
            })
        }

        composable(Signup.route) {
            SignupScreen()
        }
    }
}