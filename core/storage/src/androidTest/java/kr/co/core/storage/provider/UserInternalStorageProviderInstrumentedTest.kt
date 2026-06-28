package kr.co.core.storage.provider

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kr.co.core.storage.config.LocalStoragePathProvider
import kr.co.core.storage.testing.BaseInstrumentationTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UserInternalStorageProviderInstrumentedTest : BaseInstrumentationTest() {

    private val pathProvider = LocalStoragePathProvider()

    @After
    fun cleanAfter() {
        File(context.filesDir, pathProvider.getUserDirectoryName("default")).deleteRecursively()
        File(context.filesDir, pathProvider.getUserDirectoryName("uid-test")).deleteRecursively()
    }

    @Test
    fun getUserDirectoryCreatesAndCachesDirectoryPerUid() {
        val auth = mockk<FirebaseAuth>()
        val user = mockk<FirebaseUser>()
        var currentUser: FirebaseUser? = null
        every { user.uid } returns "uid-test"
        every { auth.currentUser } answers { currentUser }
        val provider = UserInternalStorageProvider(context, auth, pathProvider)

        val defaultDirectory = provider.getUserDirectory()
        assertEquals(pathProvider.getUserDirectoryName("default"), defaultDirectory.name)
        assertTrue(defaultDirectory.exists())
        assertSame(defaultDirectory, provider.getUserDirectory())

        currentUser = user
        val userDirectory = provider.getUserDirectory()
        assertEquals(pathProvider.getUserDirectoryName("uid-test"), userDirectory.name)
        assertTrue(userDirectory.exists())
        assertNotSame(defaultDirectory, userDirectory)
    }

    @Test
    fun deleteUserDirectoryDeletesFilesAndClearsCachedDirectory() {
        val auth = mockk<FirebaseAuth>()
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "uid-test"
        every { auth.currentUser } returns user
        val provider = UserInternalStorageProvider(context, auth, pathProvider)

        val directory = provider.getUserDirectory()
        File(directory, "file-test").writeText("value-test")

        provider.deleteUserDirectory("uid-test")

        assertFalse(directory.exists())
        val recreated = provider.getUserDirectory()
        assertTrue(recreated.exists())
        assertNotSame(directory, recreated)
    }
}
