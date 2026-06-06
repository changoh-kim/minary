package kr.co.domain.feature.setting.model


data class UserSettings(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val diarySyncEnabled: Boolean = false,
    val lastModifiedAt: Long = 0L,
)