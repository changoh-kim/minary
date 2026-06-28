package kr.co.core.datastore.app

import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.first
import kr.co.core.datastore.testing.BaseInstrumentationTest
import kr.co.core.storage.config.LocalStoragePathProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MinaryPrefsDataStoreInstrumentedTest : BaseInstrumentationTest() {

    private val pathProvider = LocalStoragePathProvider()

    @Before
    fun cleanBefore() {
        context.preferencesDataStoreFile(pathProvider.getMinaryDataStoreName()).delete()
    }

    @After
    fun cleanAfter() {
        context.preferencesDataStoreFile(pathProvider.getMinaryDataStoreName()).delete()
    }

    @Test
    fun storesAndEmitsLastSignInUid() {
        runCoreAndroidTest {
            val dataStore = MinaryPrefsDataStore(context, pathProvider)

            assertEquals("fallback-uid-test", dataStore.getLastSignInUid("fallback-uid-test"))
            dataStore.setLastSignInUid("uid-test")

            assertEquals("uid-test", dataStore.getLastSignInUid())
            assertEquals("uid-test", dataStore.getLastSignInUidFlow().first())
        }
    }
}
