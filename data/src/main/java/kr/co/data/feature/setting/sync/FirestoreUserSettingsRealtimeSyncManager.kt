package kr.co.data.feature.setting.sync

import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.co.core.di.qualifier.ApplicationScope
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.domain.feature.setting.sync.UserSettingsRealtimeSyncScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserSettingsRealtimeSyncManager @Inject constructor(
    private val firebaseAuthProvider: FirebaseAuthProvider,
    private val firebaseFirestoreProvider: FirebaseFirestoreProvider,
    @param:ApplicationScope private val scope: CoroutineScope,
    private val settingsSyncManager: FirestoreUserSettingsSyncManager
) : UserSettingsRealtimeSyncScheduler {

    private var listenerRegistration: ListenerRegistration? = null

    override suspend fun startListening() {
        if (listenerRegistration != null) return

        val userId = firebaseAuthProvider.currentUser?.uid ?: return

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
