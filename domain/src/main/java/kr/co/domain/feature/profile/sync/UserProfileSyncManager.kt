package kr.co.domain.feature.profile.sync

import kr.co.core.common.result.AppResult

interface UserProfileSyncManager {
    suspend fun syncProfile(userId: String): AppResult<Unit>
    suspend fun pushProfile(userId: String): AppResult<Unit>
    suspend fun pullProfile(userId: String): AppResult<Unit>

    suspend fun pushProfilePhoto(userId: String): AppResult<Unit>
}