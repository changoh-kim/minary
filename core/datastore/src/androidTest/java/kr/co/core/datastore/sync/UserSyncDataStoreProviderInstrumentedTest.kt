package kr.co.core.datastore.sync

import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kr.co.core.datastore.testing.BaseInstrumentationTest
import kr.co.core.storage.config.LocalStoragePathProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test

class UserSyncDataStoreProviderInstrumentedTest : BaseInstrumentationTest() {

    private val pathProvider = LocalStoragePathProvider()

    @After
    fun cleanAfter() {
        context.preferencesDataStoreFile(pathProvider.getUserSyncDataStoreName("uid-test")).delete()
    }

    @Test
    fun cachesDataStoreForCurrentUidAndDeletesFile() {
        runCoreAndroidTest {
            val auth = mockk<FirebaseAuth>()
            val user = mockk<FirebaseUser>()
            every { user.uid } returns "uid-test"
            every { auth.currentUser } returns user
            val provider = UserSyncDataStoreProvider(context, auth, pathProvider)

            val dataStore = provider.getDataStore()
            assertSame(dataStore, provider.getDataStore())

            dataStore.setLastUserDataSyncTimestamp(100L)
            assertEquals(100L, dataStore.getLastUserDataSyncTimestamp())

            provider.deleteDataStoreFile("uid-test")
            assertFalse(context.preferencesDataStoreFile(pathProvider.getUserSyncDataStoreName("uid-test")).exists())
        }
    }
}
