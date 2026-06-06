package kr.co.presentation.feature.account.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kr.co.presentation.R
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.theme.MinaryTheme

@Composable
fun WelcomeScreen(
    onSignInClicked: () -> Unit
) {
    WelcomeContent(
        onNavigateToSignIn = onSignInClicked
    )
}

@Composable
fun WelcomeContent(
    onNavigateToSignIn: () -> Unit = {}
) {
    Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier.padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.welcome),
                    style = MaterialTheme.typography.displayLarge,
                )
                Text(
                    text = stringResource(R.string.your_mind_diary),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Button(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .align(alignment = Alignment.BottomCenter),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                onClick = onNavigateToSignIn
            ) {
                Text(
                    text = stringResource(R.string.sign_in),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun WelcomeScreenPreview() {
    WelcomePreviewContent()
}

@Composable
fun WelcomePreviewContent() {
    MinaryTheme {
        WelcomeContent()
    }
}
