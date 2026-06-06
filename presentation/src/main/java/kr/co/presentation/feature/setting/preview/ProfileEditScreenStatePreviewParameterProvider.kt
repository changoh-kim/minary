package kr.co.presentation.feature.setting.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.feature.setting.viewmodel.ProfileEditScreenState

internal class ProfileEditScreenStatePreviewParameterProvider :
    PreviewParameterProvider<ProfileEditScreenState> {

    override val values: Sequence<ProfileEditScreenState> = sequenceOf(
        ProfileEditScreenState(
            userProfile = SettingPreviewData.userProfile,
            isPhotoLoading = false
        ),
        ProfileEditScreenState(
            userProfile = SettingPreviewData.emptyUserProfile,
            isPhotoLoading = false
        )
    )
}