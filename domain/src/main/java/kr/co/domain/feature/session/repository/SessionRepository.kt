package kr.co.domain.feature.session.repository

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.session.model.UserSession

interface SessionRepository {
    suspend fun getLastSignInUid(): String?
    suspend fun setLastSignInUid(uid: String)

    suspend fun getCurrentUser(): Result<UserSession, DomainError>
    suspend fun reload(): Result<UserSession, DomainError>
    fun getSessionStateStream(): Flow<UserSession?>
}