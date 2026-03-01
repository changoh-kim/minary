package kr.co.presentation.feature.auth.preview.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.feature.auth.viewmodel.SignUpScreenState


internal class SignUpPreviewDataProvider : PreviewParameterProvider<SignUpScreenState> {

    override val values: Sequence<SignUpScreenState> = sequenceOf(
        SignUpScreenState(
            signedUpUser = null,
            email = "minary@gmail.com",
            name = "minary",
            password = "passwordValue",
            confirmPassword = "passwordValue",
            isSigningUp = false
        ),
        SignUpScreenState(
            signedUpUser = null,
            email = "minary@gmail.com",
            name = "minary",
            password = "passwordValue",
            confirmPassword = "passwordValue",
            isSigningUp = true
        ),
    )
}