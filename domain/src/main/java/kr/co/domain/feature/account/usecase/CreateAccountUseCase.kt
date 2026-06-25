package kr.co.domain.feature.account.usecase

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.account.model.SignUpInfo
import kr.co.domain.feature.account.service.AccountService
import kr.co.domain.service.time.ServerTimeProvider
import javax.inject.Inject

class CreateAccountUseCase @Inject constructor(
    private val accountService: AccountService,
    private val serverTimeProvider: ServerTimeProvider,
) {
    suspend operator fun invoke(
        signUpInfo: SignUpInfo
    ): Result<Unit, DomainError> {
        val joinedAt = serverTimeProvider.now()
        return accountService.createAccount(signUpInfo, joinedAt)
    }
}