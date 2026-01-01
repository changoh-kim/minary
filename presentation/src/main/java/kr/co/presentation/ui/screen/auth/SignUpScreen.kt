package kr.co.presentation.ui.screen.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kr.co.presentation.R
import kr.co.presentation.ui.extension.getString
import kr.co.presentation.ui.theme.MinaryTheme
import kr.co.presentation.viewmodel.auth.SignUpSideEffect
import kr.co.presentation.viewmodel.auth.SignUpState
import kr.co.presentation.viewmodel.auth.SignUpViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect


@Composable
fun SignUpScreen(
    onNavigateToLoginScreen: () -> Unit,
    signupViewModel: SignUpViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state: SignUpState by signupViewModel.collectAsState()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    signupViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SignUpSideEffect.NavigateToLoginScreen -> onNavigateToLoginScreen()
            is SignUpSideEffect.ShowMsg -> scope.launch {
                snackbarHostState.showSnackbar(context.getString(sideEffect.uiText))
            }
        }
    }

    SignUpScreen(
        snackbarHostState = snackbarHostState,

        email = state.email,
        userName = state.userName,
        password = state.password,
        confirmPassword = state.confirmPassword,

        onEmailChanged = signupViewModel::onEmailChanged,
        onUserNameChanged = signupViewModel::onUserNameChanged,
        onPasswordChanged = signupViewModel::onPasswordChanged,
        onConfirmPasswordChanged = signupViewModel::onConfirmPasswordChanged,

        signUp = signupViewModel::signUp
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun SignUpScreen(
    snackbarHostState: SnackbarHostState,

    email: String,
    userName: String,
    password: String,
    confirmPassword: String,

    onEmailChanged: (String) -> Unit,
    onUserNameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,

    signUp: () -> Unit
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
                .padding(top = 24.dp)
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
                value = email,
                onValueChange = onEmailChanged,
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
                value = userName,
                onValueChange = onUserNameChanged,
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
                value = password,
                onValueChange = onPasswordChanged,
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
                value = confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = { Text(stringResource(R.string.user_confirm_password)) },
                visualTransformation = PasswordVisualTransformation()
            )

            // Sign Up Button
            Button(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                onClick = signUp
            ) {
                Text(
                    text = stringResource(R.string.sign_up),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true, locale = "ko")
@Composable
private fun SignUpScreenPreview() {
    MinaryTheme {
        SignUpScreen(
            snackbarHostState = remember { SnackbarHostState() },
            email = "",
            userName = "",
            password = "",
            confirmPassword = "",
            onEmailChanged = {},
            onUserNameChanged = {},
            onPasswordChanged = {},
            onConfirmPasswordChanged = {},
            signUp = {}
        )
    }
}