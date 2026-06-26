package kr.co.domain.feature.account.service

import kr.co.core.common.result.AppResult
import kr.co.domain.feature.account.model.Account
import kr.co.domain.feature.account.model.SignUpInfo

interface AccountService {
    suspend fun createAccount(signUpInfo: SignUpInfo, joinedAt: Long): AppResult<Unit>
    suspend fun deleteAccount(password: String): AppResult<String>
    suspend fun signIn(email: String, password: String): AppResult<Account>
    suspend fun signOut(): AppResult<Unit>
    suspend fun checkEmailAvailability(email: String): AppResult<Boolean>
}