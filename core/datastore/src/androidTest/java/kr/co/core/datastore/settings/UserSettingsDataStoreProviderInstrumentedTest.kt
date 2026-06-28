package kr.co.core.datastore.settings

import androidx.datastore.dataStoreFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kr.co.core.datastore.proto.ThemeProto
import kr.co.core.datastore.testing.BaseInstrumentationTest
import kr.co.core.storage.config.LocalStoragePathProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test

class UserSettingsDataStoreProviderInstrumentedTest : BaseInstrumentationTest() {

    private val pathProvider = LocalStoragePathProvider()

    @After
    fun cleanAfter() {
        context.dataStoreFile(pathProvider.getUserSettingsDataStoreName("uid-test")).delete()
    }

    @Test
    fun cachesSettingsDataStoreForCurrentUidAndDeletesFile() {
        runCoreAndroidTest {
            val auth = mockk<FirebaseAuth>()
            val user = mockk<FirebaseUser>()
            every { user.uid } returns "uid-test"
            every { auth.currentUser } returns user
            val provider = UserSettingsDataStoreProvider(
                context = context,
                firebaseAuth = auth,
                serializer = UserSettingsSerializer(),
                pathProvider = pathProvider,
            )

            val dataStore = provider.getDataStore()
            assertSame(dataStore, provider.getDataStore())

            dataStore.updateData {
                it.toBuilder()
                    .setTheme(ThemeProto.DARK)
                    .setDiarySyncEnabled(true)
                    .build()
            }
            assertEquals(ThemeProto.DARK, dataStore.data.first().theme)

            provider.deleteDataStoreFile("uid-test")
            assertFalse(context.dataStoreFile(pathProvider.getUserSettingsDataStoreName("uid-test")).exists())
        }
    }
}
