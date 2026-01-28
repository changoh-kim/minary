package kr.co.domain.usecase.auth

import kr.co.domain.model.auth.User
import kr.co.domain.repository.AuthRepository
import javax.inject.Inject


class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, userName: String) : Result<User> {
        return authRepository.createAccount(email, password, userName)
    }
}