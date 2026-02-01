package kr.co.domain.feature.auth.usecase

import kr.co.domain.feature.auth.model.User
import kr.co.domain.feature.auth.repository.AuthRepository
import javax.inject.Inject


class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, userName: String) : Result<User> {
        return authRepository.createAccount(email, password, userName)
    }
}