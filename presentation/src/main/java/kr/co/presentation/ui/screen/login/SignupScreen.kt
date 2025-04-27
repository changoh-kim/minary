package kr.co.presentation.ui.screen.login

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kr.co.presentation.ui.theme.MinaryTheme

@Composable
fun SignupScreen() {
    SignupScreen("Signup")
}

@Composable
fun SignupScreen(name: String) {
    Text(
        text = "Hello $name!",
    )
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    MinaryTheme {
        SignupScreen("Signup")
    }
}