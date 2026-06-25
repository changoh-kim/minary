package kr.co.domain.feature.diary.usecase.sync

import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import javax.inject.Inject

class StopAllDiarySyncUseCase @Inject constructor(
    private val diarySyncScheduler: DiarySyncScheduler,
    private val stopRealtimeDiarySync: StopRealtimeDiarySyncUseCase,
) {
    operator fun invoke() {
        diarySyncScheduler.cancelAllSync()
        stopRealtimeDiarySync()
    }
}