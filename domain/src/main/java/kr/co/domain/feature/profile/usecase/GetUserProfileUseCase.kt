package kr.co.domain.feature.profile.usecase

import kr.co.core.common.result.AppResult
import kotlinx.coroutines.flow.first
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.profile.repository.UserProfileRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(): AppResult<UserProfile> {
        return userProfileRepository.getUserProfileStream().first()
    }
}