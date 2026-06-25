package kr.co.domain.feature.setting.model

import kr.co.core.common.model.AppTheme

data class UserSettings(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val diarySyncEnabled: Boolean = false,
    val lastModifiedAt: Long = 0L,
)
