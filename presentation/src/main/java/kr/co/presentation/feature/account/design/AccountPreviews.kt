package kr.co.presentation.feature.account.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import kr.co.presentation.design.ThemePreviews
import kr.co.presentation.feature.account.preview.provider.SignInPreviewDataProvider
import kr.co.presentation.feature.account.preview.provider.SignUpPreviewDataProvider
import kr.co.presentation.feature.account.screen.AccountDeletionPreviewContent
import kr.co.presentation.feature.account.screen.SignInPreviewContent
import kr.co.presentation.feature.account.screen.SignUpContentPreview
import kr.co.presentation.feature.account.screen.WelcomePreviewContent
import kr.co.presentation.feature.account.viewmodel.SignInScreenState
import kr.co.presentation.feature.account.viewmodel.SignUpScreenState

@ThemePreviews
@Composable
private fun WelcomeScreenPreview() {
    WelcomePreviewContent()
}

@ThemePreviews
@Composable
private fun SignUpScreenPreview(
    @PreviewParameter(SignUpPreviewDataProvider::class)
    state: SignUpScreenState
) {
    SignUpContentPreview(state)
}

@ThemePreviews
@Composable
private fun SignInScreenPreview(
    @PreviewParameter(SignInPreviewDataProvider::class)
    state: SignInScreenState
) {
    SignInPreviewContent(state)
}

@ThemePreviews
@Composable
private fun AccountDeletionScreenPreview() {
    AccountDeletionPreviewContent()
}
