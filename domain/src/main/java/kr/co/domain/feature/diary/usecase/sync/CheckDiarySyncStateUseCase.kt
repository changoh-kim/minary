package kr.co.domain.feature.diary.usecase.sync

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.first
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.diary.sync.DiarySyncStateRepository
import javax.inject.Inject

class CheckDiarySyncStateUseCase @Inject constructor(
    private val diarySyncStateRepository: DiarySyncStateRepository,
) {
    suspend operator fun invoke(): Result<Boolean, DomainError> {
        val validate = diarySyncStateRepository.pendingCount.first() > 0
        return Ok(validate)
    }
}