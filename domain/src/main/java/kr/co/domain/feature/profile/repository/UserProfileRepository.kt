package kr.co.domain.feature.profile.repository

import kr.co.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.profile.model.UserProfile

interface UserProfileRepository {
    suspend fun getUserProfileStream(): Flow<AppResult<UserProfile>>

    suspend fun updateUserProfile(profile: UserProfile): AppResult<Unit>
    suspend fun updateUserProfilePhoto(uid: String, photoUrl: String, lastModifiedAt: Long): AppResult<String>
}