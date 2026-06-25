package kr.co.core.datastore.sync

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserSyncPrefsDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        private val KEY_LAST_PULL_DIARY_MODIFIED_AT =
            longPreferencesKey("last_pull_diary_modified_at")
        private val KEY_IS_INITIAL_SYNC_COMPLETED =
            booleanPreferencesKey("is_initial_sync_completed")
        private val KEY_LAST_USER_DATA_SYNC_TIMESTAMP =
            longPreferencesKey("last_user_data_sync_timestamp")
    }

    suspend fun getLastPullDiaryModifiedAt(defValue: Long = 0L) =
        dataStore.data.map { prefs -> prefs[KEY_LAST_PULL_DIARY_MODIFIED_AT] ?: defValue }.first()

    fun getLastPullDiaryModifiedAtFlow(defValue: Long? = null): Flow<Long?> =
        dataStore.data.map { prefs -> prefs[KEY_LAST_PULL_DIARY_MODIFIED_AT] ?: defValue }

    suspend fun setLastPullDiaryModifiedAt(updatedAt: Long) =
        dataStore.edit { prefs -> prefs[KEY_LAST_PULL_DIARY_MODIFIED_AT] = updatedAt }

    suspend fun isInitialSyncCompleted(): Boolean =
        dataStore.data.map { prefs -> prefs[KEY_IS_INITIAL_SYNC_COMPLETED] ?: false }.first()

    suspend fun setInitialSyncCompleted(completed: Boolean) =
        dataStore.edit { prefs -> prefs[KEY_IS_INITIAL_SYNC_COMPLETED] = completed }

    suspend fun getLastUserDataSyncTimestamp(): Long =
        dataStore.data.map { prefs -> prefs[KEY_LAST_USER_DATA_SYNC_TIMESTAMP] ?: 0L }.first()

    suspend fun setLastUserDataSyncTimestamp(timestamp: Long) =
        dataStore.edit { prefs -> prefs[KEY_LAST_USER_DATA_SYNC_TIMESTAMP] = timestamp }
}