package kr.co.domain.feature.account.usecase

import kr.co.core.common.result.AppResult
import kr.co.domain.feature.account.service.AccountService
import javax.inject.Inject

class CheckEmailAvailabilityUseCase @Inject constructor(
    private val accountService: AccountService,
) {
    suspend operator fun invoke(email: String): AppResult<Boolean> {
        return accountService.checkEmailAvailability(email)
    }
}
