package kr.co.domain.feature.setting.repository

import kr.co.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow
import kr.co.core.common.model.AppTheme
import kr.co.domain.feature.setting.model.UserSettings

interface UserSettingsRepository {
    suspend fun getUserSettingsStream(): Flow<AppResult<UserSettings>>

    fun getAppThemeStream(): Flow<AppTheme>
    suspend fun updateAppTheme(uid: String, appTheme: AppTheme, lastModifiedAt: Long): AppResult<Unit>

    fun getDiarySyncEnabledStream(): Flow<Boolean>
    suspend fun updateDiarySyncEnabled(uid: String, enabled: Boolean, lastModifiedAt: Long): AppResult<Unit>
}