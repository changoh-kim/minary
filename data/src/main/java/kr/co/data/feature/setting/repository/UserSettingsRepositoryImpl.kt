package kr.co.data.feature.setting.repository

import android.util.Log
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.data.extension.TAG
import kr.co.data.extension.toDomainError
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toAppTheme
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toThemeProto
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toUserSettings
import kr.co.data.feature.setting.source.local.UserSettingsLocalDataSource
import kr.co.data.proto.copy
import kr.co.domain.error.DomainError
import kr.co.domain.feature.setting.model.AppTheme
import kr.co.domain.feature.setting.model.UserSettings
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.feature.setting.service.sync.UserSettingsSyncScheduler
import javax.inject.Inject

class UserSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: UserSettingsLocalDataSource,
    private val settingsSyncScheduler: UserSettingsSyncScheduler,
) : UserSettingsRepository {

    override suspend fun getUserSettingsStream(): Flow<Result<UserSettings, DomainError>> =
        localDataSource.getUserSettingsFlow().map{ Ok(it.toUserSettings()) }

    override fun getAppThemeStream(): Flow<AppTheme> =
        localDataSource.getAppThemeFlow().map { it.toAppTheme() }

    override suspend fun updateAppTheme(
        uid: String,
        appTheme: AppTheme,
        lastModifiedAt: Long,
    ): Result<Unit, DomainError> =
        runSuspendCatching {
            val localSettings = localDataSource.getUserSettings()
            localDataSource.updateUserSettings(
                localSettings.copy {
                    theme = appTheme.toThemeProto()
                    this.lastModifiedAt = lastModifiedAt
                }
            )

            settingsSyncScheduler.scheduleSettingsPush()
        }
        .onErr { Log.e(TAG, "Failed to update theme preferences", it) }
        .mapError { it.toDomainError() }

    override fun getDiarySyncEnabledStream(): Flow<Boolean> =
        localDataSource.getDiarySyncEnabledFlow()

    override suspend fun updateDiarySyncEnabled(
        uid: String,
        enabled: Boolean,
        lastModifiedAt: Long,
    ): Result<Unit, DomainError> =
        runSuspendCatching {
            val localSettings = localDataSource.getUserSettings()
            localDataSource.updateUserSettings(
                localSettings.copy {
                    diarySyncEnabled = enabled
                    this.lastModifiedAt = lastModifiedAt
                }
            )

            settingsSyncScheduler.scheduleSettingsPush()
        }.onErr { Log.e(TAG, "Failed to update diary sync enabled preferences", it) }
        .mapError { it.toDomainError() }
}