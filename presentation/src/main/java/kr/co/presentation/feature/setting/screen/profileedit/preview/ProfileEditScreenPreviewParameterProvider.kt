package kr.co.presentation.feature.setting.screen.profileedit.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.feature.setting.preview.SettingPreviewData
import kr.co.presentation.feature.setting.screen.profileedit.ProfileEditScreenState

internal class ProfileEditScreenPreviewParameterProvider :
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