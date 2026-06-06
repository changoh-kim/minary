package kr.co.presentation.feature.setting.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.domain.feature.setting.model.AppTheme
import kr.co.presentation.feature.setting.model.UserSettingsUiModel
import kr.co.presentation.feature.setting.viewmodel.SettingsScreenState

internal class SettingsScreenStatePreviewParameterProvider :
    PreviewParameterProvider<SettingsScreenState> {

    override val values: Sequence<SettingsScreenState> = sequenceOf(
        SettingsScreenState(
            userProfile = SettingPreviewData.userProfile,
            userSettings = UserSettingsUiModel(
                appTheme = AppTheme.LIGHT,
                isDiarySyncEnabled = true
            )
        ),
    )
}