package kr.co.data.feature.setting.mapper

import kr.co.data.feature.setting.model.UserSettingsDto
import kr.co.core.datastore.proto.ThemeProto
import kr.co.core.datastore.proto.UserSettingsProto
import kr.co.core.common.extension.toAppTheme
import kr.co.core.common.model.AppTheme
import kr.co.domain.feature.setting.model.UserSettings

object UserSettingsMapper {
    fun UserSettingsProto.toUserSettings(): UserSettings {
        return UserSettings(
            appTheme = theme.toAppTheme(),
            diarySyncEnabled = diarySyncEnabled,
            lastModifiedAt = lastModifiedAt
        )
    }

    fun UserSettings.toUserSettingsProto(): UserSettingsProto {
        return UserSettingsProto
            .newBuilder()
            .setTheme(appTheme.toThemeProto())
            .setDiarySyncEnabled(diarySyncEnabled)
            .setLastModifiedAt(lastModifiedAt)
            .build()
    }

    fun ThemeProto.toAppTheme(): AppTheme {
        return when (this) {
            ThemeProto.LIGHT -> AppTheme.LIGHT
            ThemeProto.DARK -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        }
    }

    fun AppTheme.toThemeProto(): ThemeProto {
        return when (this) {
            AppTheme.LIGHT -> ThemeProto.LIGHT
            AppTheme.DARK -> ThemeProto.DARK
            else -> ThemeProto.SYSTEM
        }
    }

    fun UserSettingsProto.toUserSettingsDto(): UserSettingsDto {
        return UserSettingsDto(
            theme = theme.name,
            diarySyncEnabled = diarySyncEnabled,
            lastModifiedAt = lastModifiedAt
        )
    }

    fun UserSettingsDto.toUserSettingsProto(): UserSettingsProto {
        return UserSettingsProto.newBuilder()
            .setTheme(theme.toAppTheme().toThemeProto())
            .setDiarySyncEnabled(diarySyncEnabled)
            .setLastModifiedAt(lastModifiedAt)
            .build()
    }
}