package kr.co.domain.feature.account.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.account.service.AccountService
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import org.junit.jupiter.api.Test

class CheckEmailAvailabilityUseCaseTest : DomainCoroutineTest() {

    private val accountService = mockk<AccountService>()
    private val useCase = CheckEmailAvailabilityUseCase(accountService)

    @Test
    fun `returns account service result when email is available`() {
        runDomainTest {
            coEvery { accountService.checkEmailAvailability(DomainFixtures.EMAIL) } returns Ok(true)

            val result = useCase(DomainFixtures.EMAIL)

            result.assertOk(true)
            coVerify(exactly = 1) { accountService.checkEmailAvailability(DomainFixtures.EMAIL) }
        }
    }

    @Test
    fun `returns account service failure`() {
        runDomainTest {
            val error = DomainError.NetworkUnavailable
            coEvery { accountService.checkEmailAvailability(DomainFixtures.EMAIL) } returns Err(error)

            val result = useCase(DomainFixtures.EMAIL)

            result.assertErr(error)
            coVerify(exactly = 1) { accountService.checkEmailAvailability(DomainFixtures.EMAIL) }
        }
    }
}
