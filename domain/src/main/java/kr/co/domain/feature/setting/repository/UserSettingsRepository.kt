package kr.co.domain.feature.setting.repository

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kr.co.core.common.error.DomainError
import kr.co.core.common.model.AppTheme
import kr.co.domain.feature.setting.model.UserSettings

interface UserSettingsRepository {
    suspend fun getUserSettingsStream(): Flow<Result<UserSettings, DomainError>>

    fun getAppThemeStream(): Flow<AppTheme>
    suspend fun updateAppTheme(uid: String, appTheme: AppTheme, lastModifiedAt: Long): Result<Unit, DomainError>

    fun getDiarySyncEnabledStream(): Flow<Boolean>
    suspend fun updateDiarySyncEnabled(uid: String, enabled: Boolean, lastModifiedAt: Long): Result<Unit, DomainError>
}