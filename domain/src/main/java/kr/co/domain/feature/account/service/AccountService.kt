package kr.co.domain.feature.account.service

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.account.model.Account
import kr.co.domain.feature.account.model.SignUpInfo

interface AccountService {
    suspend fun createAccount(signUpInfo: SignUpInfo, joinedAt: Long): Result<Unit, DomainError>
    suspend fun deleteAccount(password: String): Result<String, DomainError>
    suspend fun signIn(email: String, password: String): Result<Account, DomainError>
    suspend fun signOut(): Result<Unit, DomainError>
    suspend fun checkEmailAvailability(email: String): Result<Boolean, DomainError>
}