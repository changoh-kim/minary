package kr.co.presentation.ui.screen.auth

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.presentation.ui.theme.MinaryTheme
import kr.co.presentation.viewmodel.auth.LoginSideEffect
import kr.co.presentation.viewmodel.auth.LoginState
import kr.co.presentation.viewmodel.auth.LoginViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect


@Composable
fun LoginScreen(
    onNavigateToMainScreen: () -> Unit,
    onNavigateToSignupScreen: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel(),
) {
    val state: LoginState by loginViewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    loginViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is LoginSideEffect.NavigateToMainScreen -> onNavigateToMainScreen()
            is LoginSideEffect.NavigateToSignupScreen -> onNavigateToSignupScreen()
            is LoginSideEffect.ShowMsg -> scope.launch {
                snackbarHostState.showSnackbar(sideEffect.msg)
            }
        }
    }

    LoginScreen(
        snackbarHostState = snackbarHostState,

        id = state.id,
        password = state.password,
        isLoggingIn = state.isLoggingIn,

        onIdChanged = loginViewModel::onIdChanged,
        onPasswordChanged = loginViewModel::onPasswordChanged,
        login = loginViewModel::login,

        onNavigateToSignupScreen = loginViewModel::onNavigateToSignupScreen
    )
}

@Composable
private fun LoginScreen(
    snackbarHostState: SnackbarHostState,

    id: String,
    password: String,
    isLoggingIn: Boolean,

    onIdChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    login: () -> Unit,

    onNavigateToSignupScreen: () -> Unit,
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
                    text = "Log in",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Id
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Id",
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = id,
                    onValueChange = onIdChanged,
                    label = { Text("User Id") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Password",
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = onPasswordChanged,
                    label = { Text("User Password") },
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoggingIn) {
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
                        onClick = login
                    ) {
                        Text(text = "Login")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 24.dp)
                            .clickable(onClick = onNavigateToSignupScreen)
                    ) {
                        Text(text = "Don't have an account?")
                        Text(text = " Sign up ", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    MinaryTheme {
        LoginScreen(
            snackbarHostState = remember { SnackbarHostState() },
            id = "",
            password = "",
            isLoggingIn = false,
            onIdChanged = {},
            onPasswordChanged = {},
            login = {},
            onNavigateToSignupScreen = {}
        )
    }
}