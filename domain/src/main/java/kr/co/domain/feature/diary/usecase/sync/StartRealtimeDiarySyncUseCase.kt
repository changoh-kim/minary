package kr.co.domain.feature.diary.usecase.sync

import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.diary.sync.DiaryRealtimeSyncManager
import javax.inject.Inject


class StartRealtimeDiarySyncUseCase @Inject constructor(
    private val realtimeSyncScheduler: DiaryRealtimeSyncManager,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        realtimeSyncScheduler.startListening()
        return Ok(Unit)
    }
}