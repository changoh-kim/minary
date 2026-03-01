package kr.co.presentation.feature.auth.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kr.co.presentation.feature.auth.preview.provider.LoginPreviewDataProvider
import kr.co.presentation.feature.auth.viewmodel.LoginAction
import kr.co.presentation.feature.auth.viewmodel.LoginScreenState
import kr.co.presentation.feature.auth.viewmodel.LoginSideEffect
import kr.co.presentation.feature.auth.viewmodel.LoginViewModel
import kr.co.presentation.theme.MinaryTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect


@Composable
fun LoginScreen(
    onLoginSucceeded: () -> Unit,
    onSignUpClicked: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state: LoginScreenState by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is LoginSideEffect.LoginSucceeded -> onLoginSucceeded()
            is LoginSideEffect.SignUpClicked -> onSignUpClicked()
            is LoginSideEffect.ShowMessage -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    LoginContent(
        email = state.email,
        password = state.password,
        isLoggingIn = state.isLoggingIn,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::handleAction
    )
}

@Composable
fun LoginContent(
    email: String = "minary@gmail.com",
    password: String = "passwordValue",
    isLoggingIn: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (LoginAction) -> Unit = {}
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
                .fillMaxWidth()
                .padding(it)
                .padding(horizontal = 24.dp),
        ) {
            Text(
                modifier = Modifier.padding(top = 36.dp, bottom = 36.dp),
                text = stringResource(R.string.login),
                style = MaterialTheme.typography.headlineMedium
            )

            // Id
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(R.string.id),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                value = email,
                onValueChange = { value -> onAction(LoginAction.EmailChanged(value)) },
                label = { Text(stringResource(R.string.user_id)) }
            )

            // Password
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(R.string.password),
                style = MaterialTheme.typography.labelLarge
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                value = password,
                onValueChange = { value -> onAction(LoginAction.PasswordChanged(value)) },
                label = { Text(stringResource(R.string.user_password)) },
                visualTransformation = PasswordVisualTransformation()
            )

            // login button
            LoadingButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                text = stringResource(R.string.btn_login),
                onClick = { onAction(LoginAction.LoginClicked) },
                isLoading = isLoggingIn
            )

            // sign up text button
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp)
                    .clickable(onClick = { onAction(LoginAction.SignUpClicked) }),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(text = stringResource(R.string.do_not_have_an_account))
                Text(
                    text = stringResource(R.string.sign_up),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun LoginContentPreview(
    @PreviewParameter(LoginPreviewDataProvider::class) state: LoginScreenState
) {
    MinaryTheme {
        LoginContent(
            state.email,
            state.password,
            state.isLoggingIn,
        )
    }
}