package kr.co.presentation.feature.setting.mapper

import kr.co.domain.feature.setting.model.UserSettings
import kr.co.presentation.feature.setting.model.UserSettingsUiModel

object UserSettingsMapper {
    fun UserSettings.toUserSettingsUiModel(): UserSettingsUiModel {
        return UserSettingsUiModel(
            appTheme = appTheme,
            isDiarySyncEnabled = diarySyncEnabled,
            lastModifiedAt = lastModifiedAt,
        )
    }

    fun UserSettingsUiModel.toUserSettings(): UserSettings {
        return UserSettings(
            appTheme = appTheme,
            diarySyncEnabled = isDiarySyncEnabled,
            lastModifiedAt = lastModifiedAt,
        )
    }
}