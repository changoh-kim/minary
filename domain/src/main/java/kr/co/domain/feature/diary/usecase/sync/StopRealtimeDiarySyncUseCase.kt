package kr.co.domain.feature.diary.usecase.sync

import kr.co.domain.feature.diary.sync.DiaryRealtimeSyncManager
import javax.inject.Inject


class StopRealtimeDiarySyncUseCase @Inject constructor(
    private val realtimeSyncScheduler: DiaryRealtimeSyncManager,
) {
    operator fun invoke(): Unit = realtimeSyncScheduler.stopListening()
}