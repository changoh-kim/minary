package kr.co.data.local.provider

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
import kr.co.data.feature.profile.serializer.UserProfileSerializer
import kr.co.data.local.config.LocalStoragePathProvider
import kr.co.data.proto.UserProfileProto
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileDataStoreProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val serializer: UserProfileSerializer,
    private val pathProvider: LocalStoragePathProvider
) {
    private val dataStores = ConcurrentHashMap<String, DataStore<UserProfileProto>>()
    private val scopes = ConcurrentHashMap<String, CoroutineScope>()

    private fun getDataStoreFile(uid: String) = context.dataStoreFile(pathProvider.getUserProfileDataStoreName(uid))

    fun getDataStore(): DataStore<UserProfileProto> {
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
