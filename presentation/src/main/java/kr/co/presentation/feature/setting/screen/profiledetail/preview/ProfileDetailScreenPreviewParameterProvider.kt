package kr.co.presentation.feature.setting.screen.profiledetail.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.feature.setting.preview.SettingPreviewData
import kr.co.presentation.feature.setting.screen.profiledetail.ProfileDetailScreenState

internal class ProfileDetailScreenPreviewParameterProvider :
    PreviewParameterProvider<ProfileDetailScreenState> {

    override val values: Sequence<ProfileDetailScreenState> = sequenceOf(
        ProfileDetailScreenState(userProfile = SettingPreviewData.userProfile),
        ProfileDetailScreenState(userProfile = SettingPreviewData.emptyUserProfile)
    )
}