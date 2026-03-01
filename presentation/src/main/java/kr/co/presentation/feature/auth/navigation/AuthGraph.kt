package kr.co.presentation.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kr.co.presentation.feature.auth.screen.LoginScreen
import kr.co.presentation.feature.auth.screen.SignUpScreen
import kr.co.presentation.feature.auth.screen.WelcomeScreen
import kr.co.presentation.navigation.LoginRoute
import kr.co.presentation.navigation.MinaryAppState
import kr.co.presentation.navigation.SignUpRoute
import kr.co.presentation.navigation.WelcomeRoute


internal fun NavGraphBuilder.authGraph(
    appState: MinaryAppState,
) {
    composable<WelcomeRoute> {
        WelcomeScreen(onLoginClicked = { appState.navigateToLogin() })
    }

    composable<LoginRoute> {
        LoginScreen(
            onLoginSucceeded = { appState.navigateToMain() },
            onSignUpClicked = { appState.navigateToSignUp() }
        )
    }

    composable<SignUpRoute> {
        SignUpScreen(onSignUpSucceeded = { appState.navigateBack() })
    }
}