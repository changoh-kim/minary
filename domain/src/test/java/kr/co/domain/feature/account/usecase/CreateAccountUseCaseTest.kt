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
import kr.co.domain.testing.FakeServerTimeProvider
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import org.junit.jupiter.api.Test

class CreateAccountUseCaseTest : DomainCoroutineTest() {

    private val accountService = mockk<AccountService>()
    private val serverTime = FakeServerTimeProvider(currentTime = DomainFixtures.FIXED_TIME)
    private val useCase = CreateAccountUseCase(accountService, serverTime)

    @Test
    fun `creates account with server time as joined at`() {
        runDomainTest {
            val signUpInfo = DomainFixtures.signUpInfo()
            coEvery { accountService.createAccount(signUpInfo, DomainFixtures.FIXED_TIME) } returns Ok(Unit)

            val result = useCase(signUpInfo)

            result.assertOk(Unit)
            coVerify(exactly = 1) {
                accountService.createAccount(signUpInfo, DomainFixtures.FIXED_TIME)
            }
        }
    }

    @Test
    fun `returns account service failure`() {
        runDomainTest {
            val error = DomainError.Auth.EmailAlreadyInUse
            val signUpInfo = DomainFixtures.signUpInfo()
            coEvery { accountService.createAccount(signUpInfo, DomainFixtures.FIXED_TIME) } returns Err(error)

            val result = useCase(signUpInfo)

            result.assertErr(error)
            coVerify(exactly = 1) {
                accountService.createAccount(signUpInfo, DomainFixtures.FIXED_TIME)
            }
        }
    }
}
