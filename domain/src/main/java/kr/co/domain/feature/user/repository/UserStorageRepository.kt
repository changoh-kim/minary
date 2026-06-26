package kr.co.domain.feature.user.repository

import kr.co.core.common.result.AppResult

interface UserStorageRepository {
    suspend fun deleteUserStorage(uid: String): AppResult<Unit>
}