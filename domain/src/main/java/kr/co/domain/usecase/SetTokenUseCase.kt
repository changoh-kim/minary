package kr.co.domain.usecase

import kr.co.domain.repository.AuthTokenRepository
import javax.inject.Inject

class SetTokenUseCase @Inject constructor(
    private val authTokenRepository: AuthTokenRepository
) {
    suspend operator fun invoke(token: String) {
        authTokenRepository.setToken(token)
    }
}