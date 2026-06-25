package kr.co.data.feature.account.service

import android.util.Log
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import kr.co.core.common.error.DomainError
import kr.co.core.common.extension.TAG
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
    private val remoteDataSource: AccountRemoteDataSource,
) : AccountService {
    override suspend fun createAccount(signUpInfo: SignUpInfo, joinedAt: Long): Result<Unit, DomainError> =
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
        .onErr { Log.e(TAG, "Failed to create account with profile", it) }
        .mapError { it.toDomainError() }

    override suspend fun deleteAccount(password: String): Result<String, DomainError> =
        runSuspendCatching {
            remoteDataSource.deleteAccount(password)
        }
        .onErr { Log.e(TAG, "Failed to delete account", it) }
        .mapError { it.toDomainError() }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<Account, DomainError> =
        runSuspendCatching {
            remoteDataSource.signIn(email, password)
        }
        .onErr { Log.e(TAG, "Failed to sign in", it) }
        .map { it.toAccount() }
        .mapError { it.toDomainError() }

    override suspend fun signOut(): Result<Unit, DomainError> =
        runSuspendCatching {
            remoteDataSource.signOut()
        }
        .onErr { Log.e(TAG, "Failed to sign out", it) }
        .mapError { it.toDomainError() }

    override suspend fun checkEmailAvailability(email: String): Result<Boolean, DomainError> =
        runSuspendCatching {
            remoteDataSource.checkEmailAvailability(email)
        }
        .onErr { Log.e(TAG, "Failed to check email availability", it) }
        .mapError { it.toDomainError() }
}