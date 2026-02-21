package kr.co.presentation.feature.auth.preview.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.feature.auth.viewmodel.SignUpScreenState


internal class SignUpPreviewDataProvider : PreviewParameterProvider<SignUpScreenState> {

    override val values: Sequence<SignUpScreenState> = sequenceOf(
        SignUpScreenState(
            signUpLoadState = LoadState.Uninitialized,
            email = "testEmail",
            name = "testName",
            password = "testPw",
            confirmPassword = "testConfirmPw"
        ),
        SignUpScreenState(
            signUpLoadState = LoadState.Loading,
            email = "testEmail",
            name = "testName",
            password = "testPw",
            confirmPassword = "testConfirmPw"
        ),
    )
}