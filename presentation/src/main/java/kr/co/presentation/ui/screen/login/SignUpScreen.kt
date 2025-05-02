package kr.co.presentation.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import kr.co.presentation.viewmodel.SignUpSideEffect
import kr.co.presentation.viewmodel.SignUpState
import kr.co.presentation.viewmodel.SignUpViewModel
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

    signupViewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is SignUpSideEffect.NavigateToLoginScreen -> onNavigateToLoginScreen()
            is SignUpSideEffect.ShowMsg -> scope.launch { snackbarHostState.showSnackbar(sideEffect.msg) }
        }
    }

    SignUpScreen(
        id = state.id,
        userName = state.userName,
        password = state.password,
        confirmPassword = state.confirmPassword,

        onIdChanged = signupViewModel::onIdChanged,
        onUserNameChanged = signupViewModel::onUserNameChanged,
        onPasswordChanged = signupViewModel::onPasswordChanged,
        onConfirmPasswordChanged = signupViewModel::onConfirmPasswordChanged,

        signUp = signupViewModel::signUp
    )
}

@Composable
private fun SignUpScreen(
    id: String,
    userName: String,
    password: String,
    confirmPassword: String,

    onIdChanged: (String) -> Unit,
    onUserNameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,

    signUp: () -> Unit
) {

    Surface {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    text = "Create an account",
                    style = MaterialTheme.typography.headlineMedium
                )

                // ID
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Id",
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = id,
                    onValueChange = onIdChanged,
                    label = { Text("User Id") },
                )

                // User Name
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Name",
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userName,
                    onValueChange = onUserNameChanged,
                    label = { Text("User Name") }
                )

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

                // Confirm Password
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Confirm password",
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChanged,
                    label = { Text("User Confirm Password") },
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
                        text = "Sign up",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    MinaryTheme {
        SignUpScreen(
            id = "",
            userName = "",
            password = "",
            confirmPassword = "",
            onIdChanged = {},
            onUserNameChanged = {},
            onPasswordChanged = {},
            onConfirmPasswordChanged = {},
            signUp = {}
        )
    }
}