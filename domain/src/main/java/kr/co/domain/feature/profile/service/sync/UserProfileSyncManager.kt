package kr.co.domain.feature.profile.service.sync

import com.github.michaelbull.result.Result
import kr.co.domain.error.DomainError

interface UserProfileSyncManager {
    suspend fun syncProfile(userId: String): Result<Unit, DomainError>
    suspend fun pushProfile(userId: String): Result<Unit, DomainError>
    suspend fun pullProfile(userId: String): Result<Unit, DomainError>

    suspend fun pushProfilePhoto(userId: String): Result<Unit, DomainError>
}