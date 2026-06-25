package kr.co.presentation.feature.setting.model

import androidx.compose.runtime.Immutable
import kr.co.core.common.model.AppTheme

@Immutable
data class UserSettingsUiModel(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val isDiarySyncEnabled: Boolean = false,
    val lastModifiedAt: Long = 0L,
)