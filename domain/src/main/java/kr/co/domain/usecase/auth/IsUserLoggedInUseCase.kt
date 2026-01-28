package kr.co.domain.usecase.auth

import kr.co.domain.model.auth.User
import kr.co.domain.repository.AuthRepository
import javax.inject.Inject


class IsUserLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.getCurrentUser()
    }
}