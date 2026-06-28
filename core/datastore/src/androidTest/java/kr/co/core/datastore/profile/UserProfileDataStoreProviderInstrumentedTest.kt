package kr.co.core.datastore.profile

import androidx.datastore.dataStoreFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kr.co.core.datastore.proto.UserProfileProto
import kr.co.core.datastore.testing.BaseInstrumentationTest
import kr.co.core.storage.config.LocalStoragePathProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test

class UserProfileDataStoreProviderInstrumentedTest : BaseInstrumentationTest() {

    private val pathProvider = LocalStoragePathProvider()

    @After
    fun cleanAfter() {
        context.dataStoreFile(pathProvider.getUserProfileDataStoreName("uid-test")).delete()
    }

    @Test
    fun cachesProfileDataStoreForCurrentUidAndDeletesFile() {
        runCoreAndroidTest {
            val auth = mockk<FirebaseAuth>()
            val user = mockk<FirebaseUser>()
            every { user.uid } returns "uid-test"
            every { auth.currentUser } returns user
            val provider = UserProfileDataStoreProvider(
                context = context,
                firebaseAuth = auth,
                serializer = UserProfileSerializer(),
                pathProvider = pathProvider,
            )

            val dataStore = provider.getDataStore()
            assertSame(dataStore, provider.getDataStore())

            dataStore.updateData {
                UserProfileProto.newBuilder(it)
                    .setUid("uid-test")
                    .setName("name-test")
                    .build()
            }
            assertEquals("uid-test", dataStore.data.first().uid)

            provider.deleteDataStoreFile("uid-test")
            assertFalse(context.dataStoreFile(pathProvider.getUserProfileDataStoreName("uid-test")).exists())
        }
    }
}
