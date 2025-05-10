package kr.co.domain.usecase

import kr.co.domain.model.User
import kr.co.domain.repository.AuthRepository
import javax.inject.Inject

class IsUserLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.getCurrentUser()
    }
}