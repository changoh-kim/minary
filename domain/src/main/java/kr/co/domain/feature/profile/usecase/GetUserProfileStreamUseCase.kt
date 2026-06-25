package kr.co.domain.feature.profile.usecase

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.profile.repository.UserProfileRepository
import javax.inject.Inject


class GetUserProfileStreamUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(): Flow<Result<UserProfile, DomainError>> {
        return userProfileRepository.getUserProfileStream().distinctUntilChanged()
    }
}