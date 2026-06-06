package kr.co.presentation.feature.setting.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.presentation.feature.setting.viewmodel.ProfileDetailScreenState

internal class ProfileDetailScreenStatePreviewParameterProvider :
    PreviewParameterProvider<ProfileDetailScreenState> {

    override val values: Sequence<ProfileDetailScreenState> = sequenceOf(
        ProfileDetailScreenState(userProfile = SettingPreviewData.userProfile),
        ProfileDetailScreenState(userProfile = SettingPreviewData.emptyUserProfile)
    )
}