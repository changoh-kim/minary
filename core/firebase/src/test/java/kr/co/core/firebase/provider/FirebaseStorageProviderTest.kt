package kr.co.core.firebase.provider

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class FirebaseStorageProviderTest {

    @Test
    fun `builds profile photo storage reference`() {
        val storage = mockk<FirebaseStorage>()
        val root = mockk<StorageReference>()
        val photoRef = mockk<StorageReference>()
        every { storage.reference } returns root
        every { root.child("profile_photos/uid-test.jpg") } returns photoRef
        val provider = FirebaseStorageProvider(storage)

        val result = provider.getUserProfilePhotoRef("uid-test")

        assertSame(photoRef, result)
    }
}
