package kr.co.domain.usecase.auth

import kr.co.domain.model.auth.User
import kr.co.domain.repository.AuthRepository
import javax.inject.Inject


class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.signIn(email, password)
    }
}
