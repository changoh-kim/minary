package kr.co.data.feature.setting.service.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.co.data.di.qualifier.ApplicationScope
import kr.co.data.remote.firebase.provider.FirebaseFirestoreProvider
import kr.co.domain.feature.setting.service.sync.UserSettingsRealtimeSyncScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserSettingsRealtimeSyncManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseFirestoreProvider: FirebaseFirestoreProvider,
    @param:ApplicationScope private val scope: CoroutineScope,
    private val settingsSyncManager: FirestoreUserSettingsSyncManager
) : UserSettingsRealtimeSyncScheduler {

    private var listenerRegistration: ListenerRegistration? = null

    override suspend fun startListening() {
        if (listenerRegistration != null) return

        val userId = auth.currentUser?.uid ?: return

        listenerRegistration =
            firebaseFirestoreProvider
                .getUserSettingsRef(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch {
                        settingsSyncManager.pullSettings(snapshot)
                    }
                }
    }

    override fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}