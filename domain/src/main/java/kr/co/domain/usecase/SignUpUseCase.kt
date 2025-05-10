package kr.co.domain.usecase

import kr.co.domain.model.User
import kr.co.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, userName: String) : Result<User> {
        return authRepository.createAccount(email, password, userName)
    }
}