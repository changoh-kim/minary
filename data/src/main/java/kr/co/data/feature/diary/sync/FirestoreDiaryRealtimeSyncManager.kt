package kr.co.data.feature.diary.sync

import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.co.core.di.qualifier.ApplicationScope
import kr.co.core.datastore.sync.UserSyncDataStoreProvider
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.data.feature.diary.model.DiaryDto
import kr.co.domain.feature.diary.sync.DiaryRealtimeSyncManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDiaryRealtimeSyncManager @Inject constructor(
    private val firebaseAuthProvider: FirebaseAuthProvider,
    private val userDataStoreProvider: UserSyncDataStoreProvider,
    private val firebaseFirestoreProvider: FirebaseFirestoreProvider,
    private val syncManager: FirestoreDiarySyncManager,
    @param:ApplicationScope private val scope: CoroutineScope
) : DiaryRealtimeSyncManager {

    private val dataStore get() = userDataStoreProvider.getDataStore()

    private var listenerRegistration: ListenerRegistration? = null

    override suspend fun startListening() {
        if (listenerRegistration != null) return

        val userId = firebaseAuthProvider.currentUser?.uid ?: return
        // 마지막 Pull 시점 이후의 데이터를 받아오기위해 구합니다.
        val lastModifiedAt = dataStore.getLastPullDiaryModifiedAt()

        listenerRegistration = firebaseFirestoreProvider
            .getDiariesRef(userId)
            .whereGreaterThan(DiaryDto.LAST_MODIFIED_AT, lastModifiedAt)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                scope.launch {
                    val remoteDiaries = snapshot.documentChanges.mapNotNull {
                        it.document.toObject(DiaryDto::class.java)
                    }

                    val latestTimestamp = syncManager.pullRemoteDiaries(remoteDiaries)

                    // 마지막 Pull 시점 저장
                    val currentSyncedAt = dataStore.getLastPullDiaryModifiedAt()
                    if (latestTimestamp > currentSyncedAt) {
                        dataStore.setLastPullDiaryModifiedAt(latestTimestamp)
                    }
                }
            }
    }

    override fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}
