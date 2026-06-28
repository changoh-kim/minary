package kr.co.core.firebase.provider

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.google.firebase.firestore.WriteBatch
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class FirebaseFirestoreProviderTest {

    private val firestore = mockk<FirebaseFirestore>()
    private val provider = FirebaseFirestoreProvider(firestore)

    @Test
    fun `builds diary collection reference under user document`() {
        val users = mockk<CollectionReference>()
        val userDocument = mockk<DocumentReference>()
        val diaries = mockk<CollectionReference>()
        every { firestore.collection("users") } returns users
        every { users.document("uid-test") } returns userDocument
        every { userDocument.collection("diaries") } returns diaries

        val result = provider.getDiariesRef("uid-test")

        assertSame(diaries, result)
    }

    @Test
    fun `builds profile and settings document references`() {
        val users = mockk<CollectionReference>()
        val userDocument = mockk<DocumentReference>()
        val profileCollection = mockk<CollectionReference>()
        val settingsCollection = mockk<CollectionReference>()
        val profileDocument = mockk<DocumentReference>()
        val settingsDocument = mockk<DocumentReference>()
        every { firestore.collection("users") } returns users
        every { users.document("uid-test") } returns userDocument
        every { userDocument.collection("profile") } returns profileCollection
        every { userDocument.collection("settings") } returns settingsCollection
        every { profileCollection.document("userProfile") } returns profileDocument
        every { settingsCollection.document("userSettings") } returns settingsDocument

        assertSame(profileDocument, provider.getUserProfileRef("uid-test"))
        assertSame(settingsDocument, provider.getUserSettingsRef("uid-test"))
    }

    @Test
    fun `delegates batch and transaction creation`() {
        val batch = mockk<WriteBatch>()
        val task = mockk<Task<String>>()
        val function = mockk<Transaction.Function<String>>()
        every { firestore.batch() } returns batch
        every { firestore.runTransaction(function) } returns task

        assertSame(batch, provider.batch())
        assertSame(task, provider.runTransaction(function))
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 1) { firestore.runTransaction(function) }
    }
}
