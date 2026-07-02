package kr.co.data.feature.diary.sync

import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kr.co.core.datastore.sync.UserSyncDataStoreProvider
import kr.co.core.datastore.sync.UserSyncPrefsDataStore
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.data.feature.diary.model.DiaryDto
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreDiaryRealtimeSyncManagerTest : BaseDataUnitTest() {
    @Test
    fun `startListening skips listener when current user is missing`() = runDataTest {
        val userDataStoreProvider = mockk<UserSyncDataStoreProvider>()
        val firebaseFirestoreProvider = mockk<FirebaseFirestoreProvider>()
        val manager = manager(
            firebaseAuthProvider = authProviderWithoutUser(),
            userDataStoreProvider = userDataStoreProvider,
            firebaseFirestoreProvider = firebaseFirestoreProvider,
            scope = this,
        )

        manager.startListening()

        verify(exactly = 0) { userDataStoreProvider.getDataStore() }
        verify(exactly = 0) { firebaseFirestoreProvider.getDiariesRef(any()) }
    }

    @Test
    fun `startListening registers query from last pull timestamp once while active`() = runDataTest {
        val harness = listenerHarness(scope = this, lastPullValues = listOf(100L))

        harness.manager.startListening()
        harness.manager.startListening()

        verify(exactly = 1) { harness.firebaseFirestoreProvider.getDiariesRef(DataFixtures.UID) }
        verify(exactly = 1) {
            harness.collectionReference.whereGreaterThan(DiaryDto.LAST_MODIFIED_AT, 100L)
        }
        verify(exactly = 1) { harness.query.addSnapshotListener(any<EventListener<QuerySnapshot>>()) }
    }

    @Test
    fun `snapshot callback pulls remote diaries and stores newer latest timestamp`() = runDataTest {
        val harness = listenerHarness(scope = this, lastPullValues = listOf(100L, 100L))
        val older = DataFixtures.diaryDto.copy(
            id = "remote-diary-older-test",
            lastModifiedAt = 200L,
        )
        val newer = DataFixtures.diaryDto.copy(
            id = "remote-diary-newer-test",
            lastModifiedAt = 500L,
        )
        coEvery { harness.syncManager.pullRemoteDiaries(listOf(older, newer)) } returns 500L

        harness.manager.startListening()
        harness.listenerSlot.captured.onEvent(querySnapshot(older, newer), null)
        advanceUntilIdle()

        coVerify(exactly = 1) { harness.syncManager.pullRemoteDiaries(listOf(older, newer)) }
        coVerify(exactly = 1) { harness.dataStore.setLastPullDiaryModifiedAt(500L) }
    }

    @Test
    fun `snapshot callback does not store latest timestamp when current timestamp is newer`() = runDataTest {
        val harness = listenerHarness(scope = this, lastPullValues = listOf(100L, 600L))
        val remote = DataFixtures.diaryDto.copy(lastModifiedAt = 500L)
        coEvery { harness.syncManager.pullRemoteDiaries(listOf(remote)) } returns 500L

        harness.manager.startListening()
        harness.listenerSlot.captured.onEvent(querySnapshot(remote), null)
        advanceUntilIdle()

        coVerify(exactly = 1) { harness.syncManager.pullRemoteDiaries(listOf(remote)) }
        coVerify(exactly = 0) { harness.dataStore.setLastPullDiaryModifiedAt(any()) }
    }

    @Test
    fun `stopListening removes active listener`() = runDataTest {
        val harness = listenerHarness(scope = this, lastPullValues = listOf(100L))

        harness.manager.startListening()
        harness.manager.stopListening()

        verify(exactly = 1) { harness.listenerRegistration.remove() }
    }

    private fun manager(
        firebaseAuthProvider: FirebaseAuthProvider,
        userDataStoreProvider: UserSyncDataStoreProvider = mockk(),
        firebaseFirestoreProvider: FirebaseFirestoreProvider = mockk(),
        scope: CoroutineScope,
        syncManager: FirestoreDiarySyncManager = mockk(),
    ) = FirestoreDiaryRealtimeSyncManager(
        firebaseAuthProvider = firebaseAuthProvider,
        userDataStoreProvider = userDataStoreProvider,
        firebaseFirestoreProvider = firebaseFirestoreProvider,
        syncManager = syncManager,
        scope = scope,
    )

    private fun listenerHarness(
        scope: CoroutineScope,
        lastPullValues: List<Long>,
    ): DiaryRealtimeHarness {
        val dataStore = mockk<UserSyncPrefsDataStore>()
        val userDataStoreProvider = mockk<UserSyncDataStoreProvider> {
            every { getDataStore() } returns dataStore
        }
        val collectionReference = mockk<CollectionReference>()
        val query = mockk<Query>()
        val listenerRegistration = mockk<ListenerRegistration>(relaxed = true)
        val listenerSlot = slot<EventListener<QuerySnapshot>>()
        val firebaseFirestoreProvider = mockk<FirebaseFirestoreProvider> {
            every { getDiariesRef(DataFixtures.UID) } returns collectionReference
        }
        val syncManager = mockk<FirestoreDiarySyncManager>()

        coEvery { dataStore.getLastPullDiaryModifiedAt() } returnsMany lastPullValues
        coEvery { dataStore.setLastPullDiaryModifiedAt(any()) } returns mockk<Preferences>()
        every {
            collectionReference.whereGreaterThan(
                DiaryDto.LAST_MODIFIED_AT,
                lastPullValues.first(),
            )
        } returns query
        every { query.addSnapshotListener(capture(listenerSlot)) } returns listenerRegistration

        return DiaryRealtimeHarness(
            manager = manager(
                firebaseAuthProvider = authProviderWithUser(),
                userDataStoreProvider = userDataStoreProvider,
                firebaseFirestoreProvider = firebaseFirestoreProvider,
                scope = scope,
                syncManager = syncManager,
            ),
            firebaseFirestoreProvider = firebaseFirestoreProvider,
            collectionReference = collectionReference,
            query = query,
            dataStore = dataStore,
            listenerRegistration = listenerRegistration,
            listenerSlot = listenerSlot,
            syncManager = syncManager,
        )
    }

    private fun querySnapshot(vararg diaries: DiaryDto): QuerySnapshot {
        val changes = diaries.map { diary ->
            val documentSnapshot = mockk<QueryDocumentSnapshot> {
                every { toObject(DiaryDto::class.java) } returns diary
            }
            val documentChange = mockk<DocumentChange>()
            every { documentChange.document } returns documentSnapshot
            documentChange
        }

        return mockk {
            every { documentChanges } returns changes
        }
    }

    private fun authProviderWithUser(uid: String = DataFixtures.UID): FirebaseAuthProvider {
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns uid

        return mockk {
            every { currentUser } returns firebaseUser
        }
    }

    private fun authProviderWithoutUser(): FirebaseAuthProvider = mockk {
        every { currentUser } returns null
    }

    private data class DiaryRealtimeHarness(
        val manager: FirestoreDiaryRealtimeSyncManager,
        val firebaseFirestoreProvider: FirebaseFirestoreProvider,
        val collectionReference: CollectionReference,
        val query: Query,
        val dataStore: UserSyncPrefsDataStore,
        val listenerRegistration: ListenerRegistration,
        val listenerSlot: CapturingSlot<EventListener<QuerySnapshot>>,
        val syncManager: FirestoreDiarySyncManager,
    )
}
