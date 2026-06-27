package kr.co.domain.feature.diary.usecase.sync

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import kr.co.domain.feature.diary.sync.DiaryRealtimeSyncManager
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import org.junit.jupiter.api.Test

class StopDiarySyncUseCasesTest {

    private val scheduler = mockk<DiarySyncScheduler>()
    private val realtimeSyncManager = mockk<DiaryRealtimeSyncManager>()

    @Test
    fun `stops realtime diary sync`() {
        every { realtimeSyncManager.stopListening() } just runs
        val useCase = StopRealtimeDiarySyncUseCase(realtimeSyncManager)

        useCase()

        verify(exactly = 1) { realtimeSyncManager.stopListening() }
    }

    @Test
    fun `cancels diary full sync`() {
        every { scheduler.cancelFullSync() } just runs
        val useCase = StopDiaryFullSyncUseCase(scheduler)

        useCase()

        verify(exactly = 1) { scheduler.cancelFullSync() }
    }

    @Test
    fun `cancels diary periodic sync`() {
        every { scheduler.cancelPeriodicSync() } just runs
        val useCase = StopDiaryPeriodicSyncUseCase(scheduler)

        useCase()

        verify(exactly = 1) { scheduler.cancelPeriodicSync() }
    }

    @Test
    fun `cancels all diary sync then stops realtime sync`() {
        every { scheduler.cancelAllSync() } just runs
        every { realtimeSyncManager.stopListening() } just runs
        val stopRealtime = StopRealtimeDiarySyncUseCase(realtimeSyncManager)
        val useCase = StopAllDiarySyncUseCase(scheduler, stopRealtime)

        useCase()

        verifyOrder {
            scheduler.cancelAllSync()
            realtimeSyncManager.stopListening()
        }
    }
}
