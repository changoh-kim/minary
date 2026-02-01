package kr.co.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject


private val Context.dataStore by preferencesDataStore(name = "minary_datastore")

class AppDataStore @Inject constructor(
    private val context: Context,
) {
    companion object {
        private val KEY_TOKEN = stringPreferencesKey("token")
    }

    suspend fun setToken(token: String) {
        context.dataStore.edit { pref ->
            pref[KEY_TOKEN] = token
        }
    }

    suspend fun getToken(): String {
        val token = context.dataStore.data.map { it[KEY_TOKEN] }.firstOrNull()
        return if (token.isNullOrBlank()) "" else token
    }

    suspend fun clearToken() {
        context.dataStore.edit { pref ->
            pref[KEY_TOKEN] = ""
        }
    }
}