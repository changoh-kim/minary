package kr.co.data.feature.session.source.local

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kr.co.core.datastore.app.MinaryPrefsDataStore
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SessionLocalDataSourceTest : BaseDataUnitTest() {
    @Test
    fun `getLastSignInUid delegates app data store`() = runDataTest {
        val appDataStore = mockk<MinaryPrefsDataStore>()
        coEvery { appDataStore.getLastSignInUid() } returns DataFixtures.UID
        val source = SessionLocalDataSource(appDataStore)

        assertEquals(DataFixtures.UID, source.getLastSignInUid())

        coVerify(exactly = 1) { appDataStore.getLastSignInUid() }
    }

    @Test
    fun `setLastSignInUid delegates app data store`() = runDataTest {
        val appDataStore = mockk<MinaryPrefsDataStore>()
        coEvery { appDataStore.setLastSignInUid(DataFixtures.UID) } returns Unit
        val source = SessionLocalDataSource(appDataStore)

        source.setLastSignInUid(DataFixtures.UID)

        coVerify(exactly = 1) { appDataStore.setLastSignInUid(DataFixtures.UID) }
    }
}
