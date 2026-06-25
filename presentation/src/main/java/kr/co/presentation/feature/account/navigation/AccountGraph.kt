package kr.co.presentation.feature.account.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kr.co.presentation.feature.account.screen.deletion.AccountDeletionScreen
import kr.co.presentation.feature.account.screen.signin.SignInScreen
import kr.co.presentation.feature.account.screen.signup.SignUpScreen
import kr.co.presentation.feature.account.screen.welcome.WelcomeScreen
import kr.co.presentation.app.navigation.MinaryAppState
import kr.co.presentation.app.navigation.route.AccountDeletionRoute
import kr.co.presentation.app.navigation.route.SignInRoute
import kr.co.presentation.app.navigation.route.SignUpRoute
import kr.co.presentation.app.navigation.route.WelcomeRoute

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
