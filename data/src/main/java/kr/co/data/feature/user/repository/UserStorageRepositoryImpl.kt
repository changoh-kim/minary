package kr.co.data.feature.user.repository

import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kr.co.data.extension.toDomainError
import kr.co.data.feature.user.source.local.UserStorageLocalDataSource
import kr.co.domain.feature.user.repository.UserStorageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStorageRepositoryImpl @Inject constructor(
    private val userStorageLocalDataSource: UserStorageLocalDataSource,
) : UserStorageRepository {

    override suspend fun deleteUserStorage(uid: String): AppResult<Unit> =
        runSuspendCatching {
            userStorageLocalDataSource.deleteUserStorage(uid)
        }.mapError { it.toDomainError() }
}