package kr.co.data.feature.user.repository

import androidx.datastore.preferences.core.Preferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kr.co.core.common.state.SyncProcessState
import kr.co.core.datastore.sync.UserSyncDataStoreProvider
import kr.co.core.datastore.sync.UserSyncPrefsDataStore
import kr.co.data.testing.BaseDataUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserDataSyncStateRepositoryImplTest : BaseDataUnitTest() {
    @Test
    fun `userDataSyncState starts idle and updates to latest state`() = runDataTest {
        val repository = repository()
        val inProgress = SyncProcessState.InProgress.Indeterminate

        assertEquals(SyncProcessState.Idle, repository.userDataSyncState.value)

        repository.updateUserDataSyncState(inProgress)
        assertEquals(inProgress, repository.userDataSyncState.value)

        repository.updateUserDataSyncState(SyncProcessState.Completed)
        assertEquals(SyncProcessState.Completed, repository.userDataSyncState.value)
    }

    @Test
    fun `last sync timestamp delegates user sync data store`() = runDataTest {
        val dataStore = mockk<UserSyncPrefsDataStore>()
        coEvery { dataStore.getLastUserDataSyncTimestamp() } returns 123L
        coEvery { dataStore.setLastUserDataSyncTimestamp(456L) } returns mockk<Preferences>()
        val repository = repository(dataStore)

        assertEquals(123L, repository.getLastSyncTimestamp())
        repository.setLastSyncTimestamp(456L)

        coVerify(exactly = 1) { dataStore.getLastUserDataSyncTimestamp() }
        coVerify(exactly = 1) { dataStore.setLastUserDataSyncTimestamp(456L) }
    }

    private fun repository(
        dataStore: UserSyncPrefsDataStore = mockk(relaxed = true),
    ): UserDataSyncStateRepositoryImpl {
        val userSyncDataStoreProvider = mockk<UserSyncDataStoreProvider> {
            every { getDataStore() } returns dataStore
        }

        return UserDataSyncStateRepositoryImpl(
            userSyncDataStoreProvider = userSyncDataStoreProvider,
        )
    }
}
