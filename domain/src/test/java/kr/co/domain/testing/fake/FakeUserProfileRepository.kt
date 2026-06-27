package kr.co.domain.testing.fake

import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.profile.repository.UserProfileRepository
import kr.co.domain.testing.DomainFixtures

data class UserProfilePhotoUpdate(
    val uid: String,
    val photoUrl: String,
    val lastModifiedAt: Long,
)

class FakeUserProfileRepository(
    initialProfile: AppResult<UserProfile> = Ok(DomainFixtures.userProfile()),
) : UserProfileRepository {
    val userProfileStream = MutableStateFlow(initialProfile)
    val updatedProfiles = mutableListOf<UserProfile>()
    val profilePhotoUpdates = mutableListOf<UserProfilePhotoUpdate>()

    var updateUserProfileResult: AppResult<Unit> = Ok(Unit)
    var updateUserProfilePhotoResult: AppResult<String> = Ok("photo-url-test")

    override suspend fun getUserProfileStream(): Flow<AppResult<UserProfile>> = userProfileStream

    override suspend fun updateUserProfile(profile: UserProfile): AppResult<Unit> {
        updatedProfiles += profile
        userProfileStream.value = Ok(profile)
        return updateUserProfileResult
    }

    override suspend fun updateUserProfilePhoto(
        uid: String,
        photoUrl: String,
        lastModifiedAt: Long,
    ): AppResult<String> {
        profilePhotoUpdates += UserProfilePhotoUpdate(uid, photoUrl, lastModifiedAt)
        return updateUserProfilePhotoResult
    }
}
