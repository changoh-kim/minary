package kr.co.presentation.feature.setting.model

import androidx.compose.runtime.Immutable
import kr.co.domain.feature.setting.model.AppTheme

@Immutable
data class UserSettingsUiModel(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val isDiarySyncEnabled: Boolean = false,
    val lastModifiedAt: Long = 0L,
)