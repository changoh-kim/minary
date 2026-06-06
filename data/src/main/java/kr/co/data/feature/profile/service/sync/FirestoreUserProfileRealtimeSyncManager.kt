package kr.co.data.feature.profile.service.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.co.data.di.qualifier.ApplicationScope
import kr.co.data.remote.firebase.provider.FirebaseFirestoreProvider
import kr.co.domain.feature.profile.service.sync.UserProfileRealtimeSyncScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserProfileRealtimeSyncManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseFirestoreProvider: FirebaseFirestoreProvider,
    @param:ApplicationScope private val scope: CoroutineScope,
    private val profileSyncManager: FirestoreUserProfileSyncManager,
) : UserProfileRealtimeSyncScheduler {

    private var listenerRegistration: ListenerRegistration? = null

    override suspend fun startListening() {
        if (listenerRegistration != null) return

        val userId = auth.currentUser?.uid ?: return

        listenerRegistration =
            firebaseFirestoreProvider
                .getUserProfileRef(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch {
                        profileSyncManager.pullProfile(userId, snapshot)
                    }
                }
    }

    override fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}