package kr.co.presentation.main.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.co.presentation.common.extension.getString
import kr.co.presentation.feature.auth.navigation.WelcomeRoute
import kr.co.presentation.main.navigation.MainRoute
import kr.co.presentation.main.viewmodel.MainActivitySideEffect
import kr.co.presentation.main.viewmodel.MainActivityViewModel
import kr.co.presentation.main.viewmodel.StartDestination
import kr.co.presentation.navigation.host.AppNaveGraph
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            MinaryTheme {
                val state by viewModel.collectAsState()
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                viewModel.collectSideEffect { sideEffect ->
                    when (sideEffect) {
                        is MainActivitySideEffect.ShowMsg -> coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
                        }
                    }
                }

                when (state.startDestination) {
                    is StartDestination.Welcome -> {
                        AppNaveGraph(
                            navController = navController,
                            startDestination = WelcomeRoute
                        )
                    }

                    is StartDestination.Main -> {
                        AppNaveGraph(
                            navController = navController,
                            startDestination = MainRoute
                        )
                    }

                    is StartDestination.Splash -> {}
                }
            }
        }
    }
}