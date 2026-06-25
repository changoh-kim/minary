package kr.co.domain.feature.profile.repository

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.profile.model.UserProfile

interface UserProfileRepository {
    suspend fun getUserProfileStream(): Flow<Result<UserProfile, DomainError>>

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit, DomainError>
    suspend fun updateUserProfilePhoto(uid: String, photoUrl: String, lastModifiedAt: Long): Result<String, DomainError>
}