package kr.co.domain.feature.user.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kr.co.core.common.error.DomainError
import kr.co.core.common.state.SyncProcessState
import kr.co.domain.feature.profile.sync.UserProfileSyncManager
import kr.co.domain.feature.setting.sync.UserSettingsSyncManager
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.FakeServerTimeProvider
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeUserDataSyncStateRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StartUserDataSyncUseCaseTest : DomainCoroutineTest() {

    private val userProfileSyncManager = mockk<UserProfileSyncManager>()
    private val userSettingsSyncManager = mockk<UserSettingsSyncManager>()
    private val serverTime = FakeServerTimeProvider(currentTime = DomainFixtures.FIXED_TIME)

    @Test
    fun `does not start sync managers when last sync is within interval`() {
        runDomainTest {
            val syncStateRepository = FakeUserDataSyncStateRepository(
                initialLastSyncTimestamp = DomainFixtures.FIXED_TIME - 1_000L,
            )
            val useCase = createUseCase(syncStateRepository)

            val result = useCase(DomainFixtures.UID)

            result.assertOk(Unit)
            assertEquals(emptyList<Long>(), syncStateRepository.savedTimestamps)
            coVerify(exactly = 0) { userProfileSyncManager.syncProfile(any()) }
            coVerify(exactly = 0) { userSettingsSyncManager.syncSettings(any()) }
        }
    }

    @Test
    fun `updates state and saves timestamp when profile and settings sync succeed`() {
        runDomainTest {
            val syncStateRepository = FakeUserDataSyncStateRepository()
            val useCase = createUseCase(syncStateRepository)
            coEvery { userProfileSyncManager.syncProfile(DomainFixtures.UID) } returns Ok(Unit)
            coEvery { userSettingsSyncManager.syncSettings(DomainFixtures.UID) } returns Ok(Unit)

            val result = useCase(DomainFixtures.UID)

            result.assertOk(Unit)
            assertEquals(
                listOf(
                    SyncProcessState.InProgress.Indeterminate,
                    SyncProcessState.Completed,
                ),
                syncStateRepository.stateUpdates,
            )
            assertEquals(listOf(DomainFixtures.FIXED_TIME), syncStateRepository.savedTimestamps)
            coVerify(exactly = 1) { userProfileSyncManager.syncProfile(DomainFixtures.UID) }
            coVerify(exactly = 1) { userSettingsSyncManager.syncSettings(DomainFixtures.UID) }
        }
    }

    @Test
    fun `marks failed and does not save timestamp when profile sync fails`() {
        runDomainTest {
            val error = DomainError.Store.PermissionDenied
            val syncStateRepository = FakeUserDataSyncStateRepository()
            val useCase = createUseCase(syncStateRepository)
            coEvery { userProfileSyncManager.syncProfile(DomainFixtures.UID) } returns Err(error)
            coEvery { userSettingsSyncManager.syncSettings(DomainFixtures.UID) } returns Ok(Unit)

            val result = useCase(DomainFixtures.UID)

            result.assertErr(error)
            assertEquals(
                listOf(
                    SyncProcessState.InProgress.Indeterminate,
                    SyncProcessState.Failed(error),
                ),
                syncStateRepository.stateUpdates,
            )
            assertEquals(emptyList<Long>(), syncStateRepository.savedTimestamps)
            coVerify(exactly = 1) { userProfileSyncManager.syncProfile(DomainFixtures.UID) }
            coVerify(exactly = 1) { userSettingsSyncManager.syncSettings(DomainFixtures.UID) }
        }
    }

    @Test
    fun `marks failed and does not save timestamp when settings sync fails`() {
        runDomainTest {
            val error = DomainError.Store.Unauthenticated
            val syncStateRepository = FakeUserDataSyncStateRepository()
            val useCase = createUseCase(syncStateRepository)
            coEvery { userProfileSyncManager.syncProfile(DomainFixtures.UID) } returns Ok(Unit)
            coEvery { userSettingsSyncManager.syncSettings(DomainFixtures.UID) } returns Err(error)

            val result = useCase(DomainFixtures.UID)

            result.assertErr(error)
            assertEquals(
                listOf(
                    SyncProcessState.InProgress.Indeterminate,
                    SyncProcessState.Failed(error),
                ),
                syncStateRepository.stateUpdates,
            )
            assertEquals(emptyList<Long>(), syncStateRepository.savedTimestamps)
            coVerify(exactly = 1) { userProfileSyncManager.syncProfile(DomainFixtures.UID) }
            coVerify(exactly = 1) { userSettingsSyncManager.syncSettings(DomainFixtures.UID) }
        }
    }

    private fun createUseCase(
        syncStateRepository: FakeUserDataSyncStateRepository,
    ): StartUserDataSyncUseCase = StartUserDataSyncUseCase(
        userProfileSyncManager = userProfileSyncManager,
        userSettingsSyncManager = userSettingsSyncManager,
        userDataSyncStateRepository = syncStateRepository,
        serverTime = serverTime,
    )
}
