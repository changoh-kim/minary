package kr.co.domain.feature.account.usecase

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.account.service.AccountService
import javax.inject.Inject

class CheckEmailAvailabilityUseCase @Inject constructor(
    private val accountService: AccountService,
) {
    suspend operator fun invoke(email: String): Result<Boolean, DomainError> {
        return accountService.checkEmailAvailability(email)
    }
}
