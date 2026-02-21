package kr.co.presentation.feature.auth.preview.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.common.state.LoadState
import kr.co.presentation.feature.auth.viewmodel.LoginScreenState


internal class LoginPreviewDataProvider : PreviewParameterProvider<LoginScreenState> {

    override val values: Sequence<LoginScreenState> = sequenceOf(
        LoginScreenState(email = "testEmail", password = "testPassword"),
        LoginScreenState(email = "testEmail", password = "testPassword", loginLoadState = LoadState.Loading),
    )
}