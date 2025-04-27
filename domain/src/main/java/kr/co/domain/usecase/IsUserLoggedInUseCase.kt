package kr.co.domain.usecase

import kr.co.domain.repository.AuthTokenRepository
import javax.inject.Inject

class IsUserLoggedInUseCase @Inject constructor(
    private val authTokenRepository: AuthTokenRepository
) {
    suspend operator fun invoke(): Boolean {
        return authTokenRepository.getToken().isNotBlank()
    }
}