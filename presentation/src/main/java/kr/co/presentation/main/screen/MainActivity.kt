package kr.co.presentation.main.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.co.presentation.R
import kr.co.presentation.common.composable.LoadStateContent
import kr.co.presentation.common.extension.getString
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.feature.auth.navigation.WelcomeRoute
import kr.co.presentation.main.navigation.MainRoute
import kr.co.presentation.main.preview.LoadStatePreviewDataProvider
import kr.co.presentation.main.viewmodel.MainActivitySideEffect
import kr.co.presentation.main.viewmodel.MainActivityViewModel
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

                LoadStateContent(
                    loadState = state.loginLoadState,
                    loading = { LoadingContent() },
                    error = { error ->
                        AppNaveGraph(
                            navController = navController,
                            startDestination = WelcomeRoute
                        )
                    },
                ) {
                    AppNaveGraph(
                        navController = navController,
                        startDestination = MainRoute
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PreviewUnInitializedContent() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.preview_uninitialized_content))
    }
}

@Composable
private fun PreviewLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.preview_loading_content))
        CircularProgressIndicator()
    }
}

@Composable
private fun PreviewWelcomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.preview_welcome_content))
    }
}

@Composable
private fun PreviewMainScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.preview_main_content))
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun MainActivityLoadStateContentPreviewPreview(
    @PreviewParameter(LoadStatePreviewDataProvider::class) loadState: LoadState<Unit>
) {
    MinaryTheme {
        LoadStateContent(
            loadState = loadState,
            uninitialized = { PreviewUnInitializedContent() },
            loading = { PreviewLoadingContent() },
            error = { PreviewWelcomeScreen() },
            content = {
                PreviewMainScreen()
            }
        )
    }
}