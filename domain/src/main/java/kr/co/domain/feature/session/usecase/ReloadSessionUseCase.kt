package kr.co.domain.feature.session.usecase

import kr.co.core.common.result.AppResult
import kr.co.domain.feature.session.model.UserSession
import kr.co.domain.feature.session.repository.SessionRepository
import javax.inject.Inject

class ReloadSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(): AppResult<UserSession> {
        return sessionRepository.reload()
    }
}