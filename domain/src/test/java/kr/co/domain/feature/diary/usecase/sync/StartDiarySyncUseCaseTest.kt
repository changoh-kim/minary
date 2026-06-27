package kr.co.domain.feature.diary.usecase.sync

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeDiarySyncStateRepository
import org.junit.jupiter.api.Test

class StartDiarySyncUseCaseTest : DomainCoroutineTest() {

    private val initialDiarySync = mockk<InitialDiarySyncUseCase>()
    private val diarySyncScheduler = mockk<DiarySyncScheduler>(relaxed = true)

    @Test
    fun `runs initial sync and schedules periodic sync when initial sync is not completed`() {
        runDomainTest {
            val syncStateRepository = FakeDiarySyncStateRepository(initialSyncCompleted = false)
            val useCase = createUseCase(syncStateRepository)
            coEvery { initialDiarySync() } returns Ok(Unit)

            val result = useCase()

            result.assertOk(Unit)
            coVerify(exactly = 1) { initialDiarySync() }
            verify(exactly = 0) { diarySyncScheduler.scheduleFullSync() }
            verify(exactly = 1) { diarySyncScheduler.schedulePeriodicSync() }
        }
    }

    @Test
    fun `returns initial sync failure and does not schedule sync work`() {
        runDomainTest {
            val error = DomainError.Store.PermissionDenied
            val syncStateRepository = FakeDiarySyncStateRepository(initialSyncCompleted = false)
            val useCase = createUseCase(syncStateRepository)
            coEvery { initialDiarySync() } returns Err(error)

            val result = useCase()

            result.assertErr(error)
            coVerify(exactly = 1) { initialDiarySync() }
            verify(exactly = 0) { diarySyncScheduler.scheduleFullSync() }
            verify(exactly = 0) { diarySyncScheduler.schedulePeriodicSync() }
        }
    }

    @Test
    fun `schedules full and periodic sync when initial sync is completed`() {
        runDomainTest {
            val syncStateRepository = FakeDiarySyncStateRepository(initialSyncCompleted = true)
            val useCase = createUseCase(syncStateRepository)

            val result = useCase()

            result.assertOk(Unit)
            coVerify(exactly = 0) { initialDiarySync() }
            verify(exactly = 1) { diarySyncScheduler.scheduleFullSync() }
            verify(exactly = 1) { diarySyncScheduler.schedulePeriodicSync() }
        }
    }

    private fun createUseCase(
        syncStateRepository: FakeDiarySyncStateRepository,
    ): StartDiarySyncUseCase = StartDiarySyncUseCase(
        syncStateRepository = syncStateRepository,
        initialDiarySync = initialDiarySync,
        diarySyncScheduler = diarySyncScheduler,
    )
}
