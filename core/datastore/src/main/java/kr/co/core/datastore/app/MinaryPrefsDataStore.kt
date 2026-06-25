package kr.co.core.datastore.app

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kr.co.core.storage.config.LocalStoragePathProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MinaryPrefsDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val localStoragePathProvider: LocalStoragePathProvider
) {
    companion object {
        private val KEY_LAST_SIGN_IN_UID = stringPreferencesKey("last_sign_in_uid")
    }

    private val dataStore by lazy {
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(localStoragePathProvider.getMinaryDataStoreName()) }
        )
    }

    suspend fun getLastSignInUid(defValue: String? = null): String? =
        dataStore.data.map { prefs -> prefs[KEY_LAST_SIGN_IN_UID] ?: defValue }.first()

    fun getLastSignInUidFlow(defValue: String? = null): Flow<String?> =
        dataStore.data.map { prefs -> prefs[KEY_LAST_SIGN_IN_UID] ?: defValue }

    suspend fun setLastSignInUid(uid: String): Unit {
        dataStore.edit { prefs -> prefs[KEY_LAST_SIGN_IN_UID] = uid }
    }
}