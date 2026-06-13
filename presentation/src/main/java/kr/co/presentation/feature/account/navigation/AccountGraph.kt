package kr.co.presentation.feature.account.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kr.co.presentation.feature.account.screen.AccountDeletionScreen
import kr.co.presentation.feature.account.screen.SignInScreen
import kr.co.presentation.feature.account.screen.SignUpScreen
import kr.co.presentation.feature.account.screen.WelcomeScreen
import kr.co.presentation.navigation.AccountDeletionRoute
import kr.co.presentation.navigation.MinaryAppState
import kr.co.presentation.navigation.SignInRoute
import kr.co.presentation.navigation.SignUpRoute
import kr.co.presentation.navigation.WelcomeRoute

fun NavGraphBuilder.accountGraph(
    appState: MinaryAppState,
) {
    composable<WelcomeRoute> {
        WelcomeScreen(onSignInClicked = { appState.navigateToSignIn() })
    }

    composable<SignInRoute> {
        SignInScreen(
            onSignInSucceeded = { appState.navigateToHome() },
            onSignUpClicked = { appState.navigateToSignUp() }
        )
    }

    composable<SignUpRoute> {
        SignUpScreen(
            onSignUpSucceeded = { appState.navigateBack() },
            onBackClicked = { appState.navigateBack() }
        )
    }

    composable<AccountDeletionRoute> {
        AccountDeletionScreen(
            onBackClicked = { appState.navigateBack() },
            onDeletionSucceeded = { appState.navigateToWelcome() }
        )
    }
}
