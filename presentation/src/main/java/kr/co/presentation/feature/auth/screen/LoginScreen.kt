package kr.co.presentation.feature.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.presentation.R
import kr.co.presentation.common.extension.getString
import kr.co.presentation.theme.MinaryTheme
import kr.co.presentation.feature.auth.viewmodel.LoginIntent
import kr.co.presentation.feature.auth.viewmodel.LoginSideEffect
import kr.co.presentation.feature.auth.viewmodel.LoginUiState
import kr.co.presentation.feature.auth.viewmodel.LoginViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect


@Composable
fun LoginScreen(
    onNavigateToMainScreen: () -> Unit,
    onNavigateToSignUpScreen: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state: LoginUiState by viewModel.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is LoginSideEffect.NavigateToMainScreen -> onNavigateToMainScreen()
            is LoginSideEffect.NavigateToSignUpScreen -> onNavigateToSignUpScreen()
            is LoginSideEffect.ShowMsg -> coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    LoginContent(
        state = state,
        snackbarHostState = snackbarHostState,
        intent = viewModel::handleIntent
    )
}

@Composable
fun LoginContent(
    state: LoginUiState = LoginUiState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    intent: (LoginIntent) -> Unit = {}
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
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    modifier = Modifier.padding(top = 36.dp),
                    text = stringResource(R.string.login),
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Id
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(R.string.id),
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.id,
                    onValueChange = { value -> intent(LoginIntent.IdChanged(value)) },
                    label = { Text(stringResource(R.string.user_id)) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = stringResource(R.string.password),
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.password,
                    onValueChange = { value -> intent(LoginIntent.PasswordChanged(value)) },
                    label = { Text(stringResource(R.string.user_password)) },
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (state.isLoggingIn) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        onClick = { intent(LoginIntent.LoginButtonClicked) }
                    ) {
                        Text(text = stringResource(R.string.btn_login))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 24.dp)
                            .clickable(onClick = { intent(LoginIntent.SignUpButtonClicked) })
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
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun LoginContentPreview() {
    MinaryTheme {
        LoginContent()
    }
}