package kr.co.domain.feature.profile.usecase

import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.coroutines.coroutineBinding
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.profile.repository.UserProfileRepository
import kr.co.domain.feature.session.repository.SessionRepository
import kr.co.domain.service.time.ServerTimeProvider
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userProfileRepository: UserProfileRepository,
    private val serverTimeProvider: ServerTimeProvider,
) {
    suspend operator fun invoke(userProfile: UserProfile): AppResult<Unit> =
        coroutineBinding {
            val user = sessionRepository.getCurrentUser().bind()

            userProfileRepository.updateUserProfile(
                userProfile.copy(
                    uid = user.uid,
                    lastModifiedAt = serverTimeProvider.now(),
                )
            ).bind()
        }
}
