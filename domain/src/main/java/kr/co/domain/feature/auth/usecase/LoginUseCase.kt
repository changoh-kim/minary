package kr.co.domain.feature.auth.usecase

import kr.co.domain.feature.auth.model.User
import kr.co.domain.feature.auth.repository.AuthRepository
import javax.inject.Inject


class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.signIn(email, password)
    }
}