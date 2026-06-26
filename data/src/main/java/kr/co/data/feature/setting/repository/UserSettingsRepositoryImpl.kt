package kr.co.data.feature.setting.repository

import android.util.Log
import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.core.common.extension.TAG
import kr.co.data.extension.toDomainError
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toAppTheme
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toThemeProto
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toUserSettings
import kr.co.data.feature.setting.source.local.UserSettingsLocalDataSource
import kr.co.core.datastore.proto.copy
import kr.co.core.common.model.AppTheme
import kr.co.domain.feature.setting.model.UserSettings
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.feature.setting.sync.UserSettingsSyncScheduler
import javax.inject.Inject

class UserSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: UserSettingsLocalDataSource,
    private val settingsSyncScheduler: UserSettingsSyncScheduler,
) : UserSettingsRepository {

    override suspend fun getUserSettingsStream(): Flow<AppResult<UserSettings>> =
        localDataSource.getUserSettingsFlow().map{ Ok(it.toUserSettings()) }

    override fun getAppThemeStream(): Flow<AppTheme> =
        localDataSource.getAppThemeFlow().map { it.toAppTheme() }

    override suspend fun updateAppTheme(
        uid: String,
        appTheme: AppTheme,
        lastModifiedAt: Long,
    ): AppResult<Unit> =
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
    ): AppResult<Unit> =
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