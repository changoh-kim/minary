package kr.co.data.feature.profile.sync

import com.github.michaelbull.result.Ok
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
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
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirestoreUserProfileRealtimeSyncManagerTest : BaseDataUnitTest() {
    @Test
    fun `startListening skips listener when current user is missing`() = runDataTest {
        val firebaseFirestoreProvider = mockk<FirebaseFirestoreProvider>()
        val manager = manager(
            firebaseAuthProvider = authProviderWithoutUser(),
            firebaseFirestoreProvider = firebaseFirestoreProvider,
            scope = this,
        )

        manager.startListening()

        verify(exactly = 0) { firebaseFirestoreProvider.getUserProfileRef(any()) }
    }

    @Test
    fun `startListening registers a single listener while active`() = runDataTest {
        val harness = listenerHarness(scope = this)

        harness.manager.startListening()
        harness.manager.startListening()

        verify(exactly = 1) { harness.firebaseFirestoreProvider.getUserProfileRef(DataFixtures.UID) }
        verify(exactly = 1) { harness.documentReference.addSnapshotListener(any<EventListener<DocumentSnapshot>>()) }
    }

    @Test
    fun `snapshot callback delegates uid and snapshot to profile sync manager`() = runDataTest {
        val harness = listenerHarness(scope = this)
        val snapshot = mockk<DocumentSnapshot>()
        coEvery { harness.syncManager.pullProfile(DataFixtures.UID, snapshot) } returns Ok(Unit)

        harness.manager.startListening()
        harness.listenerSlot.captured.onEvent(snapshot, null)
        advanceUntilIdle()

        coVerify(exactly = 1) { harness.syncManager.pullProfile(DataFixtures.UID, snapshot) }
    }

    @Test
    fun `stopListening removes active listener`() = runDataTest {
        val harness = listenerHarness(scope = this)

        harness.manager.startListening()
        harness.manager.stopListening()

        verify(exactly = 1) { harness.listenerRegistration.remove() }
    }

    private fun manager(
        firebaseAuthProvider: FirebaseAuthProvider,
        firebaseFirestoreProvider: FirebaseFirestoreProvider = mockk(),
        scope: CoroutineScope,
        syncManager: FirestoreUserProfileSyncManager = mockk(),
    ) = FirestoreUserProfileRealtimeSyncManager(
        firebaseAuthProvider = firebaseAuthProvider,
        firebaseFirestoreProvider = firebaseFirestoreProvider,
        scope = scope,
        profileSyncManager = syncManager,
    )

    private fun listenerHarness(scope: CoroutineScope): ProfileRealtimeHarness {
        val documentReference = mockk<DocumentReference>()
        val listenerRegistration = mockk<ListenerRegistration>(relaxed = true)
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val firebaseFirestoreProvider = mockk<FirebaseFirestoreProvider> {
            every { getUserProfileRef(DataFixtures.UID) } returns documentReference
        }
        val syncManager = mockk<FirestoreUserProfileSyncManager>()

        every { documentReference.addSnapshotListener(capture(listenerSlot)) } returns listenerRegistration

        return ProfileRealtimeHarness(
            manager = manager(
                firebaseAuthProvider = authProviderWithUser(),
                firebaseFirestoreProvider = firebaseFirestoreProvider,
                scope = scope,
                syncManager = syncManager,
            ),
            firebaseFirestoreProvider = firebaseFirestoreProvider,
            documentReference = documentReference,
            listenerRegistration = listenerRegistration,
            listenerSlot = listenerSlot,
            syncManager = syncManager,
        )
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

    private data class ProfileRealtimeHarness(
        val manager: FirestoreUserProfileRealtimeSyncManager,
        val firebaseFirestoreProvider: FirebaseFirestoreProvider,
        val documentReference: DocumentReference,
        val listenerRegistration: ListenerRegistration,
        val listenerSlot: CapturingSlot<EventListener<DocumentSnapshot>>,
        val syncManager: FirestoreUserProfileSyncManager,
    )
}
