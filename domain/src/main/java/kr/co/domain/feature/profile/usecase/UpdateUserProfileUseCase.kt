package kr.co.domain.feature.profile.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kr.co.domain.error.DomainError
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.profile.repository.UserProfileRepository
import kr.co.domain.feature.profile.util.ProfilePhotoUriConverter
import kr.co.domain.feature.session.repository.SessionRepository
import kr.co.domain.feature.time.service.ServerTimeProvider
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userProfileRepository: UserProfileRepository,
    private val uriConverter: ProfilePhotoUriConverter,
    private val serverTimeProvider: ServerTimeProvider,
) {
    suspend operator fun invoke(userProfile: UserProfile): Result<Unit, DomainError> =
        coroutineBinding {
            val user = sessionRepository.getCurrentUser().bind()
            val extractionUrl =
                uriConverter.extractPathFromTempUri(userProfile.profilePhotoUrl)
                    ?: userProfile.profilePhotoUrl

            userProfileRepository.updateUserProfile(
                userProfile.copy(
                    uid = user.uid,
                    profilePhotoUrl = extractionUrl,
                    lastModifiedAt = serverTimeProvider.now(),
                )
            ).bind()
        }
}