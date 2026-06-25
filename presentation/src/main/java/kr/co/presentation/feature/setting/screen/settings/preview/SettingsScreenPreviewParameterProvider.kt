package kr.co.presentation.feature.setting.screen.settings.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kr.co.core.common.model.AppTheme
import kr.co.presentation.feature.setting.model.UserSettingsUiModel
import kr.co.presentation.feature.setting.preview.SettingPreviewData
import kr.co.presentation.feature.setting.screen.settings.SettingsScreenState

internal class SettingsScreenPreviewParameterProvider :
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