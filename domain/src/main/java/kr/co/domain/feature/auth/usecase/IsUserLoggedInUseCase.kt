package kr.co.domain.feature.auth.usecase

import kr.co.domain.feature.auth.model.User
import kr.co.domain.feature.auth.repository.AuthRepository
import javax.inject.Inject


class IsUserLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.getCurrentUser()
    }
}