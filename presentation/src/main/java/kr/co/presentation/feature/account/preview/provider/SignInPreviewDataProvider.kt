package kr.co.presentation.feature.account.preview.provider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.feature.account.viewmodel.SignInScreenState

internal class SignInPreviewDataProvider : PreviewParameterProvider<SignInScreenState> {

    override val values: Sequence<SignInScreenState> = sequenceOf(
        SignInScreenState(),
    )
}
