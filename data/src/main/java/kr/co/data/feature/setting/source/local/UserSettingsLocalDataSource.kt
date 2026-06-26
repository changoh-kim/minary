package kr.co.data.feature.setting.source.local

import kr.co.core.common.logging.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kr.co.core.datastore.settings.UserSettingsDataStoreProvider
import kr.co.core.datastore.proto.ThemeProto
import kr.co.core.datastore.proto.UserSettingsProto
import javax.inject.Inject

class UserSettingsLocalDataSource @Inject constructor(
    private val logger: AppLogger,
    private val provider: UserSettingsDataStoreProvider,
) {
    private val dataStore get() = provider.getDataStore()

    fun getUserSettingsFlow(): Flow<UserSettingsProto> {
        return dataStore.data.catch {
            logger.e(it, "Failed to read user settings from DataStore")
            emit(UserSettingsProto.getDefaultInstance())
        }
    }

    suspend fun getUserSettings(): UserSettingsProto {
        return dataStore.data.first()
    }

    fun getAppThemeFlow(): Flow<ThemeProto> {
        return dataStore.data.map { it.theme }
    }

    fun getDiarySyncEnabledFlow(): Flow<Boolean> {
        return dataStore.data.map { it.diarySyncEnabled }
    }

    suspend fun updateUserSettings(settings: UserSettingsProto) {
        dataStore.updateData { settings }
    }
}