package kr.co.domain.feature.diary.usecase.sync

import kr.co.domain.feature.diary.service.sync.DiarySyncScheduler
import javax.inject.Inject

class StopDiaryPeriodicSyncUseCase @Inject constructor(
    private val diarySyncScheduler: DiarySyncScheduler
) {
    operator fun invoke(): Unit = diarySyncScheduler.cancelPeriodicSync()
}