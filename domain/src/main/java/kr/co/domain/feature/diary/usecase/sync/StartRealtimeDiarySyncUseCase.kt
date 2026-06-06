package kr.co.domain.feature.diary.usecase.sync

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kr.co.domain.error.DomainError
import kr.co.domain.feature.diary.service.sync.DiaryRealtimeSyncManager
import javax.inject.Inject


class StartRealtimeDiarySyncUseCase @Inject constructor(
    private val realtimeSyncScheduler: DiaryRealtimeSyncManager,
) {
    suspend operator fun invoke(): Result<Unit, DomainError> {
        realtimeSyncScheduler.startListening()
        return Ok(Unit)
    }
}