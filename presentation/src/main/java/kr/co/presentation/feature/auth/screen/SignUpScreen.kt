package kr.co.presentation.feature.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.presentation.R
import kr.co.presentation.common.composable.LoadingButton
import kr.co.presentation.common.extension.getString
import kr.co.presentation.feature.auth.preview.provider.SignUpPreviewDataProvider
import kr.co.presentation.feature.auth.viewmodel.SignUpIntent
import kr.co.presentation.feature.auth.viewmodel.SignUpSideEffect
import kr.co.presentation.feature.auth.viewmodel.SignUpScreenState
import kr.co.presentation.feature.auth.viewmodel.SignUpViewModel
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect


@Composable
fun SignUpScreen(
    onNavigateToLoginScreen: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state: SignUpScreenState by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SignUpSideEffect.NavigateToLoginScreen -> onNavigateToLoginScreen()
            is SignUpSideEffect.ShowMsg -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    SignUpContent(
        state = state,
        snackbarHostState = snackbarHostState,
        intent = viewModel::handleIntent
    )
}

@Composable
fun SignUpContent(
    state: SignUpScreenState = SignUpScreenState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    intent: (SignUpIntent) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .fillMaxHeight()
        ) {
            Text(
                modifier = Modifier.padding(top = 36.dp),
                text = stringResource(R.string.create_an_account),
                style = MaterialTheme.typography.headlineMedium
            )

            // Email
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.email),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.email,
                onValueChange = { value ->
                    intent(SignUpIntent.EmailChanged(value))
                },
                label = { Text(stringResource(R.string.user_email)) },
            )

            // User Name
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.name),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.name,
                onValueChange = { value ->
                    intent(SignUpIntent.NameChanged(value))
                },
                label = { Text(stringResource(R.string.user_name)) }
            )

            // Password
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.password),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.password,
                onValueChange = { value ->
                    intent(SignUpIntent.PasswordChanged(value))
                },
                label = { Text(stringResource(R.string.user_password)) },
                visualTransformation = PasswordVisualTransformation()
            )

            // Confirm Password
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(R.string.confirm_password),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.confirmPassword,
                onValueChange = { value ->
                    intent(SignUpIntent.ConfirmPasswordChanged(value))
                },
                label = { Text(stringResource(R.string.user_confirm_password)) },
                visualTransformation = PasswordVisualTransformation()
            )

            // sign up button
            LoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                text = stringResource(R.string.sign_up),
                onClick = { intent(SignUpIntent.SignUpButtonClicked) },
                isLoading = state.isSigningUp
            )
        }
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun SignUpContentPreview(
    @PreviewParameter(SignUpPreviewDataProvider::class) state: SignUpScreenState
) {
    MinaryTheme {
        SignUpContent(state)
    }
}