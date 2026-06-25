package kr.co.domain.feature.session.usecase

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.session.model.UserSession
import kr.co.domain.feature.session.repository.SessionRepository
import javax.inject.Inject

class ReloadSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(): Result<UserSession, DomainError> {
        return sessionRepository.reload()
    }
}