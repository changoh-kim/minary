package kr.co.domain.feature.profile.usecase

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.first
import kr.co.domain.error.DomainError
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.profile.repository.UserProfileRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(): Result<UserProfile, DomainError> {
        return userProfileRepository.getUserProfileStream().first()
    }
}