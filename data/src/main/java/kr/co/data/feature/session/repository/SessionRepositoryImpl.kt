package kr.co.data.feature.session.repository

import android.util.Log
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.core.common.extension.TAG
import kr.co.data.extension.toDomainError
import kr.co.data.feature.session.mapper.UserSessionMapper.toUserSession
import kr.co.data.feature.session.source.local.SessionLocalDataSource
import kr.co.data.feature.session.source.remote.SessionRemoteDataSource
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.session.model.UserSession
import kr.co.domain.feature.session.repository.SessionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val localDataSource: SessionLocalDataSource,
    private val remoteDataSource: SessionRemoteDataSource,
) : SessionRepository {

    override suspend fun getLastSignInUid(): String? =
        localDataSource.getLastSignInUid()

    override suspend fun setLastSignInUid(uid: String) =
        localDataSource.setLastSignInUid(uid)

    override suspend fun getCurrentUser(): Result<UserSession, DomainError> =
        runSuspendCatching {
            remoteDataSource.getCurrentUser()
        }
        .onErr { Log.e(TAG, "Failed to get current user", it) }
        .map { it.toUserSession() }
        .mapError { it.toDomainError() }

    override suspend fun reload(): Result<UserSession, DomainError> =
        runSuspendCatching {
            remoteDataSource.reload()
        }
        .onErr { Log.e(TAG, "Failed to reload current user", it) }
        .map { it.toUserSession() }
        .mapError { it.toDomainError() }

    override fun getSessionStateStream(): Flow<UserSession?> =
        remoteDataSource.observeSessionStateFlow().map { it?.toUserSession() }
}
