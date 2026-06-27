package kr.co.domain.testing.fake

import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.session.model.UserSession
import kr.co.domain.feature.session.repository.SessionRepository
import kr.co.domain.testing.DomainFixtures

class FakeSessionRepository(
    initialSession: UserSession? = DomainFixtures.userSession(),
    initialLastSignInUid: String? = null,
    var currentUserResult: AppResult<UserSession> = Ok(DomainFixtures.userSession()),
    var reloadResult: AppResult<UserSession> = Ok(DomainFixtures.userSession()),
) : SessionRepository {
    val sessionState = MutableStateFlow(initialSession)
    val setLastSignInUidCalls = mutableListOf<String>()

    var lastSignInUid: String? = initialLastSignInUid

    override suspend fun getLastSignInUid(): String? = lastSignInUid

    override suspend fun setLastSignInUid(uid: String) {
        setLastSignInUidCalls += uid
        lastSignInUid = uid
    }

    override suspend fun getCurrentUser(): AppResult<UserSession> = currentUserResult

    override suspend fun reload(): AppResult<UserSession> = reloadResult

    override fun getSessionStateStream(): Flow<UserSession?> = sessionState
}
