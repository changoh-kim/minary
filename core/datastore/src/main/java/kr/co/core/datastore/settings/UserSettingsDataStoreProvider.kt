package kr.co.core.datastore.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kr.co.core.storage.config.LocalStoragePathProvider
import kr.co.core.datastore.proto.UserSettingsProto
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsDataStoreProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val serializer: UserSettingsSerializer,
    private val pathProvider: LocalStoragePathProvider
) {
    private val dataStores = ConcurrentHashMap<String, DataStore<UserSettingsProto>>()
    private val scopes = ConcurrentHashMap<String, CoroutineScope>()

    private fun getDataStoreFile(uid: String) =
        context.dataStoreFile(pathProvider.getUserSettingsDataStoreName(uid))

    fun getDataStore(): DataStore<UserSettingsProto> {
        val uid = firebaseAuth.currentUser?.uid ?: "default"

        return dataStores.getOrPut(uid) {
            val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            scopes[uid] = scope
            
            DataStoreFactory.create(
                serializer = serializer,
                scope = scope,
                produceFile = { getDataStoreFile(uid) }
            )
        }
    }

    @Synchronized
    fun deleteDataStoreFile(uid: String) {
        scopes.remove(uid)?.cancel()

        dataStores.remove(uid)

        val file = getDataStoreFile(uid)
        if (file.exists()) file.delete()
    }
}