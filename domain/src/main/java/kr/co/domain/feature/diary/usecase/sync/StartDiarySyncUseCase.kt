package kr.co.domain.feature.diary.usecase.sync

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.onErr
import kr.co.domain.feature.diary.sync.DiarySyncStateRepository
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import javax.inject.Inject

class StartDiarySyncUseCase @Inject constructor(
    private val syncStateRepository: DiarySyncStateRepository,
    private val initialDiarySync: InitialDiarySyncUseCase,
    private val diarySyncScheduler: DiarySyncScheduler
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val isInitialSyncCompleted = syncStateRepository.isInitialSyncCompleted()

        if (!isInitialSyncCompleted) {
            initialDiarySync().onErr { return Err(it) }
        } else {
            diarySyncScheduler.scheduleFullSync()
        }

        /*diarySyncScheduler.testForceRun()*/
        diarySyncScheduler.schedulePeriodicSync()
        return Ok(Unit)
    }
}