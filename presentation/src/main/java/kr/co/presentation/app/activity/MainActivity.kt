package kr.co.presentation.app.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dagger.hilt.android.AndroidEntryPoint
import kr.co.core.ui.common.load.LoadState
import kr.co.presentation.app.MinaryApp

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.handleAction(MainActivityAction.OnResumed)
            }
        })

        // userSession에 따라 splash 화면 유지
        splashScreen.setKeepOnScreenCondition {
            viewModel.container.stateFlow.value.userSession is LoadState.Uninitialized ||
            viewModel.container.stateFlow.value.userSession is LoadState.Loading
        }

        setContent {
            MinaryApp(viewModel = viewModel)
        }
    }
}