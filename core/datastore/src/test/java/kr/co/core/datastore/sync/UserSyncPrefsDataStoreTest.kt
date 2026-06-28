package kr.co.core.datastore.sync

import kotlinx.coroutines.flow.first
import kr.co.core.datastore.testing.BaseUnitTest
import kr.co.core.datastore.testing.fake.FakePreferencesDataStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UserSyncPrefsDataStoreTest : BaseUnitTest() {

    @Test
    fun `returns default values before preferences are written`() {
        runCoreTest {
            val dataStore = UserSyncPrefsDataStore(FakePreferencesDataStore())

            assertEquals(0L, dataStore.getLastPullDiaryModifiedAt())
            assertEquals(null, dataStore.getLastPullDiaryModifiedAtFlow().first())
            assertFalse(dataStore.isInitialSyncCompleted())
            assertEquals(0L, dataStore.getLastUserDataSyncTimestamp())
        }
    }

    @Test
    fun `stores and emits sync preference values`() {
        runCoreTest {
            val dataStore = UserSyncPrefsDataStore(FakePreferencesDataStore())

            dataStore.setLastPullDiaryModifiedAt(100L)
            dataStore.setInitialSyncCompleted(true)
            dataStore.setLastUserDataSyncTimestamp(200L)

            assertEquals(100L, dataStore.getLastPullDiaryModifiedAt())
            assertEquals(100L, dataStore.getLastPullDiaryModifiedAtFlow().first())
            assertTrue(dataStore.isInitialSyncCompleted())
            assertEquals(200L, dataStore.getLastUserDataSyncTimestamp())
        }
    }
}
