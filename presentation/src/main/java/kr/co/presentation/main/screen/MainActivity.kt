package kr.co.presentation.main.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dagger.hilt.android.AndroidEntryPoint
import kr.co.domain.infra.remote.model.ServiceStatus
import kr.co.presentation.R
import kr.co.presentation.common.extension.getString
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.main.model.UserSessionUiModel
import kr.co.presentation.main.preview.MainActivityPreviewDataProvider
import kr.co.presentation.main.viewmodel.MainActivityAction
import kr.co.presentation.main.viewmodel.MainActivitySideEffect
import kr.co.presentation.main.viewmodel.MainActivityViewModel
import kr.co.presentation.navigation.HomeRoute
import kr.co.presentation.navigation.MinaryNavHost
import kr.co.presentation.navigation.WelcomeRoute
import kr.co.presentation.navigation.rememberAppState
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

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

            MinaryTheme(appTheme = state.appTheme) {
                when (val blockedState = state.serviceStatus) {
                    is ServiceStatus.Maintenance -> {
                        MaintenanceContent(reason = blockedState.reason)
                    }
                    ServiceStatus.Active -> {
                        MainActivityContent(state.userSession) { startDestination ->
                            MinaryNavHost(appState, startDestination)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainActivityContent(
    loadState: LoadState<UserSessionUiModel?>,
    content: @Composable (startDestination: Any) -> Unit
) {
    when (loadState) {
        is LoadState.Uninitialized,
        is LoadState.Loading -> LoadingContent()

        else -> {
            val startDestination =
                if (loadState is LoadState.Success) {
                    if (loadState.data != null) HomeRoute else WelcomeRoute
                } else WelcomeRoute

            content(startDestination)
        }
    }
}

@Composable
fun MaintenanceContent(
    reason: String
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Engineering,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.system_maintenance_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = reason.ifBlank { stringResource(id = R.string.system_maintenance_reason) },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
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

@ThemePreviews
@Composable
private fun MaintenanceContentPreview() {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MaintenanceContent("")
        }
    }
}

@ThemePreviews
@Composable
private fun MainActivityPreview(
    @PreviewParameter(MainActivityPreviewDataProvider::class) loadState: LoadState<UserSessionUiModel>
) {
    MinaryTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
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
}