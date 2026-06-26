package kr.co.domain.feature.session.repository

import kr.co.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.session.model.UserSession

interface SessionRepository {
    suspend fun getLastSignInUid(): String?
    suspend fun setLastSignInUid(uid: String)

    suspend fun getCurrentUser(): AppResult<UserSession>
    suspend fun reload(): AppResult<UserSession>
    fun getSessionStateStream(): Flow<UserSession?>
}