package kr.co.domain.feature.session.usecase

import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.session.model.UserSession
import kr.co.domain.feature.session.repository.SessionRepository
import javax.inject.Inject

class GetSessionStateStreamUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    operator fun invoke(): Flow<UserSession?> {
        return sessionRepository.getSessionStateStream()
    }
}