package kr.co.presentation.feature.account.screen.signin.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.feature.account.screen.signin.SignInScreenState

internal class SignInScreenPreviewParameterProvider :
    PreviewParameterProvider<SignInScreenState> {

    override val values: Sequence<SignInScreenState> = sequenceOf(
        SignInScreenState(),
    )
}