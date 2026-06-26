package kr.co.domain.feature.diary.usecase.sync

import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import kotlinx.coroutines.flow.first
import kr.co.domain.feature.diary.sync.DiarySyncStateRepository
import javax.inject.Inject

class CheckDiarySyncStateUseCase @Inject constructor(
    private val diarySyncStateRepository: DiarySyncStateRepository,
) {
    suspend operator fun invoke(): AppResult<Boolean> {
        val validate = diarySyncStateRepository.pendingCount.first() > 0
        return Ok(validate)
    }
}