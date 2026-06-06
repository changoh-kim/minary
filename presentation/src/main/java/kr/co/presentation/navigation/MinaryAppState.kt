package kr.co.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
): MinaryAppState {
    return remember(
        navController,
        coroutineScope
    ) {
        MinaryAppState(
            navController,
            coroutineScope,
            snackbarHostState,
        )
    }
}

@Stable
class MinaryAppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    fun navigateToWelcome() {
        navController.navigate(WelcomeRoute) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateToSignIn() {
        navController.navigate(SignInRoute) {
            launchSingleTop = true
        }
    }

    fun navigateToSignUp() {
        navController.navigate(SignUpRoute) {
            launchSingleTop = true
        }
    }

    fun navigateToAccountDeletion() {
        navController.navigate(AccountDeletionRoute) {
            launchSingleTop = true
        }
    }

    fun navigateToHome() {
        navController.navigate(HomeRoute) {
            popUpTo(WelcomeRoute) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateToDiary(date: LocalDate) {
        navController.navigate(
            DiaryRoute(
                date.year,
                date.monthValue,
                date.dayOfMonth,
            )
        ) {
            launchSingleTop = true
        }
    }

    fun navigateBack() = navController.popBackStack()

    fun showMessage(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
}