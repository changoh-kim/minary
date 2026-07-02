package kr.co.data.firebase

import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.tasks.await
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.core.firebase.provider.FirebaseFunctionsProvider
import kr.co.core.firebase.provider.FirebaseStorageProvider
import kr.co.data.feature.account.source.remote.AccountRemoteDataSource
import kr.co.data.testing.BaseFirebaseEmulatorTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirebaseEmulatorIntegrationSmokeTest : BaseFirebaseEmulatorTest() {

    @Test
    fun authAndFirestoreEmulatorAcceptOwnedDiaryPath() = runDataAndroidTest {
        val user = createSignedInEmulatorUser()
        val authProvider = FirebaseAuthProvider(firebaseAuth)
        assertEquals(user.uid, authProvider.currentUser?.uid)

        val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
        val diaryId = uniqueEmulatorId("diary-id")
        val diaryRef = firestoreProvider
            .getDiariesRef(user.uid)
            .document(diaryId)
        val diaryData = mapOf(
            "id" to diaryId,
            "date" to "2026-06-28",
            "title" to "title-test",
            "content" to "content-test",
            "createdAt" to 100L,
            "lastModifiedAt" to 200L,
            "syncStatus" to "SYNCED",
        )

        diaryRef.set(diaryData).await()

        val snapshot = diaryRef.get().await()
        assertTrue(snapshot.exists())
        assertEquals("title-test", snapshot.getString("title"))

        diaryRef.delete().await()
        assertFalse(diaryRef.get().await().exists())
    }

    @Test
    fun storageEmulatorAcceptsOwnProfilePhotoPathAndMetadata() = runDataAndroidTest {
        val user = createSignedInEmulatorUser()
        val storageProvider = FirebaseStorageProvider(firebaseStorage)
        val photoRef = storageProvider.getUserProfilePhotoRef(user.uid)
        val imageBytes = byteArrayOf(
            0xFF.toByte(),
            0xD8.toByte(),
            0xFF.toByte(),
            0xD9.toByte(),
        )
        val metadata = storageMetadata {
            contentType = "image/jpeg"
            setCustomMetadata("lastModifiedAt", "200")
        }

        photoRef.putBytes(imageBytes, metadata).await()

        val remoteMetadata = photoRef.metadata.await()
        assertEquals("image/jpeg", remoteMetadata.contentType)
        assertEquals("200", remoteMetadata.getCustomMetadata("lastModifiedAt"))
        assertArrayEquals(imageBytes, photoRef.getBytes(1024).await())
    }

    @Test
    fun accountRemoteDataSourceReachesCheckEmailAvailabilityCallable() = runDataAndroidTest {
        val remoteDataSource = AccountRemoteDataSource(
            firebaseAuthProvider = FirebaseAuthProvider(firebaseAuth),
            firebaseFunctionsProvider = FirebaseFunctionsProvider(firebaseFunctions),
        )

        try {
            assertTrue(
                remoteDataSource.checkEmailAvailability(
                    "${uniqueEmulatorId("callable-user")}@example.test"
                )
            )
        } catch (exception: FirebaseFunctionsException) {
            assertEquals(FirebaseFunctionsException.Code.UNAVAILABLE, exception.code)
        } catch (throwable: Throwable) {
            fail("Unexpected callable failure: ${throwable.describeForAssertion()}")
        }

        assertNotNull(FirebaseFunctionsProvider(firebaseFunctions).getCheckEmailAvailabilityCallable())
    }

    private fun Throwable.describeForAssertion(): String =
        generateSequence(this) { it.cause }
            .joinToString(separator = " <- ") { throwable ->
                "${throwable::class.java.simpleName}: ${throwable.message}"
            }
}
