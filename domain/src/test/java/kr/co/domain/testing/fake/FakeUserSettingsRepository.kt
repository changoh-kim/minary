package kr.co.domain.testing.fake

import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kr.co.core.common.model.AppTheme
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.setting.model.UserSettings
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.testing.DomainFixtures

data class AppThemeUpdate(
    val uid: String,
    val appTheme: AppTheme,
    val lastModifiedAt: Long,
)

data class DiarySyncEnabledUpdate(
    val uid: String,
    val enabled: Boolean,
    val lastModifiedAt: Long,
)

class FakeUserSettingsRepository(
    initialSettings: AppResult<UserSettings> = Ok(DomainFixtures.userSettings()),
    initialAppTheme: AppTheme = AppTheme.SYSTEM,
    initialDiarySyncEnabled: Boolean = false,
) : UserSettingsRepository {
    val userSettingsStream = MutableStateFlow(initialSettings)
    val appThemeStream = MutableStateFlow(initialAppTheme)
    val diarySyncEnabledStream = MutableStateFlow(initialDiarySyncEnabled)

    val appThemeUpdates = mutableListOf<AppThemeUpdate>()
    val diarySyncEnabledUpdates = mutableListOf<DiarySyncEnabledUpdate>()

    var updateAppThemeResult: AppResult<Unit> = Ok(Unit)
    var updateDiarySyncEnabledResult: AppResult<Unit> = Ok(Unit)

    override suspend fun getUserSettingsStream(): Flow<AppResult<UserSettings>> = userSettingsStream

    override fun getAppThemeStream(): Flow<AppTheme> = appThemeStream

    override suspend fun updateAppTheme(
        uid: String,
        appTheme: AppTheme,
        lastModifiedAt: Long,
    ): AppResult<Unit> {
        appThemeUpdates += AppThemeUpdate(uid, appTheme, lastModifiedAt)
        appThemeStream.value = appTheme
        return updateAppThemeResult
    }

    override fun getDiarySyncEnabledStream(): Flow<Boolean> = diarySyncEnabledStream

    override suspend fun updateDiarySyncEnabled(
        uid: String,
        enabled: Boolean,
        lastModifiedAt: Long,
    ): AppResult<Unit> {
        diarySyncEnabledUpdates += DiarySyncEnabledUpdate(uid, enabled, lastModifiedAt)
        diarySyncEnabledStream.value = enabled
        return updateDiarySyncEnabledResult
    }
}
