package kr.co.data.feature.account.service

import kr.co.core.common.logging.AppLogger
import kr.co.core.common.result.AppResult
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import kr.co.data.extension.toDomainError
import kr.co.data.feature.account.mapper.AccountMapper.toAccount
import kr.co.data.feature.account.source.remote.AccountRemoteDataSource
import kr.co.domain.feature.account.model.Account
import kr.co.domain.feature.account.model.SignUpInfo
import kr.co.domain.feature.account.service.AccountService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAccountService @Inject constructor(
    private val logger: AppLogger,
    private val remoteDataSource: AccountRemoteDataSource,
) : AccountService {
    override suspend fun createAccount(signUpInfo: SignUpInfo, joinedAt: Long): AppResult<Unit> =
        runSuspendCatching {
            remoteDataSource.createAccount(
                email = signUpInfo.email,
                password = signUpInfo.password,
                name = signUpInfo.name,
                gender = signUpInfo.gender.name,
                birthday = signUpInfo.birthday.toString(),
                address = signUpInfo.address,
                phoneNumber = signUpInfo.phoneNumber,
                joinedAt = joinedAt,
            )
        }
        .onErr { logger.e(it, "Failed to create account with profile") }
        .mapError { it.toDomainError() }

    override suspend fun deleteAccount(password: String): AppResult<String> =
        runSuspendCatching {
            remoteDataSource.deleteAccount(password)
        }
        .onErr { logger.e(it, "Failed to delete account") }
        .mapError { it.toDomainError() }

    override suspend fun signIn(
        email: String,
        password: String
    ): AppResult<Account> =
        runSuspendCatching {
            remoteDataSource.signIn(email, password)
        }
        .onErr { logger.e(it, "Failed to sign in") }
        .map { it.toAccount() }
        .mapError { it.toDomainError() }

    override suspend fun signOut(): AppResult<Unit> =
        runSuspendCatching {
            remoteDataSource.signOut()
        }
        .onErr { logger.e(it, "Failed to sign out") }
        .mapError { it.toDomainError() }

    override suspend fun checkEmailAvailability(email: String): AppResult<Boolean> =
        runSuspendCatching {
            remoteDataSource.checkEmailAvailability(email)
        }
        .onErr { logger.e(it, "Failed to check email availability") }
        .mapError { it.toDomainError() }
}