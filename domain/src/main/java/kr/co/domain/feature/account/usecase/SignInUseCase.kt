package kr.co.domain.feature.account.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.onErr
import kr.co.domain.error.DomainError
import kr.co.domain.feature.account.model.Account
import kr.co.domain.feature.account.service.AccountService
import kr.co.domain.feature.user.usecase.StartUserDataSyncUseCase
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val accountService: AccountService,
    private val startUserDataSync: StartUserDataSyncUseCase,
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<Account, DomainError> = coroutineBinding {
        val account = accountService.signIn(email, password).bind()

        startUserDataSync(account.uid).onErr {
            accountService.signOut()
        }.bind()

        account
    }
}