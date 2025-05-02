package kr.co.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kr.co.presentation.ui.navigation.AppRoute.Auth
import kr.co.presentation.ui.navigation.AppRoute.Main
import kr.co.presentation.ui.navigation.host.RootNaveGraph
import kr.co.presentation.ui.theme.MinaryTheme
import kr.co.presentation.viewmodel.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            MinaryTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf(Auth.route) }

                val isLoggedIn by mainViewModel.isLoggedIn.collectAsState()
                startDestination = if (isLoggedIn) Main.route else Auth.route

                RootNaveGraph(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }
}