package kr.co.domain.feature.diary.usecase.sync

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kr.co.core.common.error.DomainError
import kr.co.core.common.state.SyncProcessState
import kr.co.domain.feature.diary.sync.DiarySyncManager
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeDiarySyncStateRepository
import kr.co.domain.testing.fake.FakeSessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InitialDiarySyncUseCaseTest : DomainCoroutineTest() {

    private val diarySyncManager = mockk<DiarySyncManager>()

    @Test
    fun `updates progress and marks completed when initial pull succeeds`() {
        runDomainTest {
            val syncStateRepository = FakeDiarySyncStateRepository()
            val sessionRepository = FakeSessionRepository()
            val useCase = createUseCase(syncStateRepository, sessionRepository)
            coEvery {
                diarySyncManager.performInitialPull(DomainFixtures.UID, any())
            } answers {
                @Suppress("UNCHECKED_CAST")
                val onProgress = args[1] as (Float) -> Unit
                onProgress(0.25f)
                onProgress(1f)
                Ok(Unit)
            }

            val result = useCase()

            result.assertOk(Unit)
            assertEquals(
                listOf(
                    SyncProcessState.InProgress.Determinate(0f),
                    SyncProcessState.InProgress.Determinate(0.25f),
                    SyncProcessState.InProgress.Determinate(1f),
                    SyncProcessState.Completed,
                ),
                syncStateRepository.stateUpdates,
            )
            assertEquals(listOf(true), syncStateRepository.setInitialSyncCompletedCalls)
            coVerify(exactly = 1) {
                diarySyncManager.performInitialPull(DomainFixtures.UID, any())
            }
        }
    }

    @Test
    fun `marks failed and does not pull diaries when current user lookup fails`() {
        runDomainTest {
            val error = DomainError.Auth.UserNotFound
            val syncStateRepository = FakeDiarySyncStateRepository()
            val sessionRepository = FakeSessionRepository(currentUserResult = Err(error))
            val useCase = createUseCase(syncStateRepository, sessionRepository)

            val result = useCase()

            result.assertErr(error)
            assertEquals(
                listOf(
                    SyncProcessState.InProgress.Determinate(0f),
                    SyncProcessState.Failed(error),
                ),
                syncStateRepository.stateUpdates,
            )
            assertEquals(emptyList<Boolean>(), syncStateRepository.setInitialSyncCompletedCalls)
            coVerify(exactly = 0) { diarySyncManager.performInitialPull(any(), any()) }
        }
    }

    @Test
    fun `marks failed and does not complete when initial pull fails`() {
        runDomainTest {
            val error = DomainError.NetworkUnavailable
            val syncStateRepository = FakeDiarySyncStateRepository()
            val sessionRepository = FakeSessionRepository()
            val useCase = createUseCase(syncStateRepository, sessionRepository)
            coEvery {
                diarySyncManager.performInitialPull(DomainFixtures.UID, any())
            } answers {
                @Suppress("UNCHECKED_CAST")
                val onProgress = args[1] as (Float) -> Unit
                onProgress(0.5f)
                Err(error)
            }

            val result = useCase()

            result.assertErr(error)
            assertEquals(
                listOf(
                    SyncProcessState.InProgress.Determinate(0f),
                    SyncProcessState.InProgress.Determinate(0.5f),
                    SyncProcessState.Failed(error),
                ),
                syncStateRepository.stateUpdates,
            )
            assertEquals(emptyList<Boolean>(), syncStateRepository.setInitialSyncCompletedCalls)
            coVerify(exactly = 1) {
                diarySyncManager.performInitialPull(DomainFixtures.UID, any())
            }
        }
    }

    private fun createUseCase(
        syncStateRepository: FakeDiarySyncStateRepository,
        sessionRepository: FakeSessionRepository,
    ): InitialDiarySyncUseCase = InitialDiarySyncUseCase(
        syncStateRepository = syncStateRepository,
        sessionRepository = sessionRepository,
        diarySyncManager = diarySyncManager,
    )
}
