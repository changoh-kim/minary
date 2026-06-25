package kr.co.domain.feature.profile.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.profile.repository.UserProfileRepository
import kr.co.domain.feature.session.repository.SessionRepository
import kr.co.domain.service.time.ServerTimeProvider
import javax.inject.Inject

class UpdateUserProfilePhotoUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userProfileRepository: UserProfileRepository,
    private val serverTimeProvider: ServerTimeProvider,
) {
    suspend operator fun invoke(photoUrl: String): Result<String, DomainError> =
        coroutineBinding {
            val user = sessionRepository.getCurrentUser().bind()

            userProfileRepository.updateUserProfilePhoto(
                user.uid,
                photoUrl,
                serverTimeProvider.now()
            ).bind()
        }
}
