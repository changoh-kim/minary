package kr.co.data.feature.diary.service.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.co.data.di.qualifier.ApplicationScope
import kr.co.data.feature.diary.model.DiaryDto
import kr.co.data.local.provider.UserSyncDataStoreProvider
import kr.co.data.remote.firebase.provider.FirebaseFirestoreProvider
import kr.co.domain.feature.diary.service.sync.DiaryRealtimeSyncManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDiaryRealtimeSyncManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val userDataStoreProvider: UserSyncDataStoreProvider,
    private val firebaseFirestoreProvider: FirebaseFirestoreProvider,
    private val syncManager: FirestoreDiarySyncManager,
    @ApplicationScope private val scope: CoroutineScope
) : DiaryRealtimeSyncManager {

    private val dataStore get() = userDataStoreProvider.getDataStore()

    private var listenerRegistration: ListenerRegistration? = null

    override suspend fun startListening() {
        if (listenerRegistration != null) return

        val userId = auth.currentUser?.uid ?: return
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