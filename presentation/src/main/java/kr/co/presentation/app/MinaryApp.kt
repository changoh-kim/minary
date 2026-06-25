package kr.co.presentation.app

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
import kr.co.core.ui.common.load.LoadState
import kr.co.core.ui.common.text.getString
import kr.co.core.ui.design.preview.ThemePreviews
import kr.co.core.ui.design.theme.MinaryTheme
import kr.co.domain.service.remoteconfig.model.ServiceStatus
import kr.co.presentation.R
import kr.co.presentation.app.activity.MainActivitySideEffect
import kr.co.presentation.app.activity.MainActivityState
import kr.co.presentation.app.activity.MainActivityViewModel
import kr.co.presentation.app.model.UserSessionUiModel
import kr.co.presentation.app.navigation.MinaryAppState
import kr.co.presentation.app.navigation.MinaryNavHost
import kr.co.presentation.app.navigation.rememberAppState
import kr.co.presentation.app.navigation.route.HomeRoute
import kr.co.presentation.app.navigation.route.WelcomeRoute
import kr.co.presentation.app.preview.MinaryAppPreviewParameterProvider
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MinaryApp(
    viewModel: MainActivityViewModel
) {
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

    MinaryAppContent(
        state = state,
        appState = appState,
    )
}

@Composable
private fun MinaryAppContent(
    state: MainActivityState,
    appState: MinaryAppState,
    navHost: @Composable (startDestination: Any) -> Unit = { startDestination ->
        MinaryNavHost(appState, startDestination)
    },
) {
    MinaryTheme(appTheme = state.appTheme) {
        when (val serviceStatus = state.serviceStatus) {
            is ServiceStatus.Maintenance -> MaintenanceContent(reason = serviceStatus.reason)
            ServiceStatus.Active -> {
                val startDestination = state.userSession.toStartDestination()
                if (startDestination == null) {
                    LoadingContent()
                } else {
                    navHost(startDestination)
                }
            }
        }
    }
}

private fun LoadState<UserSessionUiModel?>.toStartDestination(): Any? {
    return when (this) {
        is LoadState.Uninitialized,
        is LoadState.Loading -> null

        is LoadState.Success -> {
            if (data != null) HomeRoute else WelcomeRoute
        }

        else -> WelcomeRoute
    }
}

@Composable
private fun MaintenanceContent(
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
private fun MinaryAppContentPreview(
    @PreviewParameter(MinaryAppPreviewParameterProvider::class)
    state: MainActivityState
) {
    MinaryAppContent(
        state = state,
        appState = rememberAppState(),
    ) { startDestination ->
        Surface(color = MaterialTheme.colorScheme.background) {
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
