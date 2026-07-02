package kr.co.data.feature.diary.sync

import androidx.datastore.preferences.core.Preferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kr.co.core.common.state.SyncProcessState
import kr.co.core.database.dao.DiaryDao
import kr.co.core.database.database.UserDatabase
import kr.co.core.database.provider.UserDatabaseProvider
import kr.co.core.datastore.sync.UserSyncDataStoreProvider
import kr.co.core.datastore.sync.UserSyncPrefsDataStore
import kr.co.data.testing.BaseDataUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DiarySyncStateRepositoryImplTest : BaseDataUnitTest() {
    @Test
    fun `initDiarySyncState starts idle and updates to latest state`() = runDataTest {
        val repository = repository()
        val inProgress = SyncProcessState.InProgress.Determinate(0.5f)

        assertEquals(SyncProcessState.Idle, repository.initDiarySyncState.value)

        repository.updateInitDiarySyncState(inProgress)
        assertEquals(inProgress, repository.initDiarySyncState.value)

        repository.updateInitDiarySyncState(SyncProcessState.Completed)
        assertEquals(SyncProcessState.Completed, repository.initDiarySyncState.value)
    }

    @Test
    fun `pendingCount delegates diary dao flow`() = runDataTest {
        val pendingCount = MutableStateFlow(0)
        val repository = repository(pendingCount = pendingCount)

        assertEquals(0, repository.pendingCount.first())

        pendingCount.value = 3
        assertEquals(3, repository.pendingCount.first())
    }

    @Test
    fun `initial sync completed delegates user sync data store`() = runDataTest {
        val dataStore = mockk<UserSyncPrefsDataStore>()
        coEvery { dataStore.isInitialSyncCompleted() } returns true
        coEvery { dataStore.setInitialSyncCompleted(false) } returns mockk<Preferences>()
        val repository = repository(dataStore = dataStore)

        assertEquals(true, repository.isInitialSyncCompleted())
        repository.setInitialSyncCompleted(false)

        coVerify(exactly = 1) { dataStore.isInitialSyncCompleted() }
        coVerify(exactly = 1) { dataStore.setInitialSyncCompleted(false) }
    }

    private fun repository(
        pendingCount: MutableStateFlow<Int> = MutableStateFlow(0),
        dataStore: UserSyncPrefsDataStore = mockk(relaxed = true),
    ): DiarySyncStateRepositoryImpl {
        val diaryDao = mockk<DiaryDao> {
            every { getPendingItemCountFlow() } returns pendingCount
        }
        val database = mockk<UserDatabase> {
            every { diaryDao() } returns diaryDao
        }
        val userDatabaseProvider = mockk<UserDatabaseProvider> {
            every { getDatabase() } returns database
        }
        val userSyncDataStoreProvider = mockk<UserSyncDataStoreProvider> {
            every { getDataStore() } returns dataStore
        }

        return DiarySyncStateRepositoryImpl(
            userDatabaseProvider = userDatabaseProvider,
            userSyncDataStoreProvider = userSyncDataStoreProvider,
        )
    }
}
