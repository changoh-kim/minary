package kr.co.domain.feature.user.repository

import com.github.michaelbull.result.Result
import kr.co.domain.error.DomainError

interface UserStorageRepository {
    suspend fun deleteUserStorage(uid: String): Result<Unit, DomainError>
}