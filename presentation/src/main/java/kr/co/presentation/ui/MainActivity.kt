package kr.co.presentation.ui

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
import kr.co.presentation.ui.extension.getString
import kr.co.presentation.ui.navigation.host.RootNaveGraph
import kr.co.presentation.ui.navigation.route.AuthGraph
import kr.co.presentation.ui.navigation.route.MainGraph
import kr.co.presentation.ui.theme.MinaryTheme
import kr.co.presentation.viewmodel.main.MainActivitySideEffect
import kr.co.presentation.viewmodel.main.MainActivityUiState
import kr.co.presentation.viewmodel.main.MainActivityViewModel
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

                when (state.uiState) {
                    is MainActivityUiState.Auth -> {
                        RootNaveGraph(
                            navController = navController,
                            startDestination = AuthGraph
                        )
                    }
                    is MainActivityUiState.Main -> {
                        RootNaveGraph(
                            navController = navController,
                            startDestination = MainGraph
                        )
                    }
                    else -> { }
                }
            }
        }
    }
}