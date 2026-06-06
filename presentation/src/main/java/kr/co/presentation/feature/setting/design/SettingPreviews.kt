package kr.co.presentation.feature.setting.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import kr.co.presentation.common.model.UiText
import kr.co.presentation.design.MinaryPreviews
import kr.co.presentation.feature.setting.preview.ProfileDetailScreenStatePreviewParameterProvider
import kr.co.presentation.feature.setting.preview.ProfileEditScreenStatePreviewParameterProvider
import kr.co.presentation.feature.setting.preview.SettingsScreenStatePreviewParameterProvider
import kr.co.presentation.feature.setting.preview.SignOutConfirmationDialogPreviewDataProvider
import kr.co.presentation.feature.setting.screen.ProfileDetailPreviewContent
import kr.co.presentation.feature.setting.screen.ProfileEditPreviewContent
import kr.co.presentation.feature.setting.screen.SettingsScreenPreviewContent
import kr.co.presentation.feature.setting.screen.SignOutConfirmationDialogPreviewContent
import kr.co.presentation.feature.setting.viewmodel.ProfileDetailScreenState
import kr.co.presentation.feature.setting.viewmodel.ProfileEditScreenState
import kr.co.presentation.feature.setting.viewmodel.SettingsScreenState


@MinaryPreviews
@Composable
fun SettingsPreviews(
    @PreviewParameter(SettingsScreenStatePreviewParameterProvider::class)
    state: SettingsScreenState
) {
    SettingsScreenPreviewContent(state = state)
}

@MinaryPreviews
@Composable
fun SignOutConfirmationDialogPreview(
    @PreviewParameter(SignOutConfirmationDialogPreviewDataProvider::class)
    uiText: UiText
) {
    SignOutConfirmationDialogPreviewContent(uiText = uiText)
}

@MinaryPreviews
@Composable
fun ProfileDetailScreenPreview(
    @PreviewParameter(ProfileDetailScreenStatePreviewParameterProvider::class)
    state: ProfileDetailScreenState
) {
    ProfileDetailPreviewContent(state = state)
}

@MinaryPreviews
@Composable
fun ProfileEditScreenPreview(
    @PreviewParameter(ProfileEditScreenStatePreviewParameterProvider::class)
    state: ProfileEditScreenState
) {
    ProfileEditPreviewContent(state = state)
}