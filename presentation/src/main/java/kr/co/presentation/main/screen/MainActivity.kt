package kr.co.presentation.main.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kr.co.presentation.common.extension.getString
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.feature.auth.model.UserUiModel
import kr.co.presentation.main.preview.MainActivityPreviewDataProvider
import kr.co.presentation.main.viewmodel.MainActivitySideEffect
import kr.co.presentation.main.viewmodel.MainActivityViewModel
import kr.co.presentation.navigation.MinaryNavHost
import kr.co.presentation.navigation.MainRoute
import kr.co.presentation.navigation.WelcomeRoute
import kr.co.presentation.navigation.rememberAppState
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
                val appState = rememberAppState()
                val context = LocalContext.current

                viewModel.collectSideEffect { sideEffect ->
                    when (sideEffect) {
                        is MainActivitySideEffect.ShowMessage -> appState.showMessage(
                            context.getString(sideEffect.uiText)
                        )
                    }
                }

                MainActivityContent(state.userLoadState) { startDestination ->
                    MinaryNavHost(appState, startDestination)
                }
            }
        }
    }
}

@Composable
fun MainActivityContent(
    loadState: LoadState<UserUiModel>,
    content: @Composable (startDestination: Any) -> Unit
) {
    when (loadState) {
        is LoadState.Uninitialized,
        is LoadState.Loading -> LoadingContent()

        else -> {
            val startDestination =
                if (loadState is LoadState.Success) MainRoute
                else WelcomeRoute

            content(startDestination)
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// 2. 안전해진 Preview
@Preview(showBackground = true, locale = "ko")
@Composable
private fun MainActivityPreview(
    @PreviewParameter(MainActivityPreviewDataProvider::class) loadState: LoadState<UserUiModel>
) {
    MinaryTheme {
        MainActivityContent(loadState = loadState) { startDestination ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "미리보기 화면: $startDestination"
                )
            }
        }
    }
}