package kr.co.domain.feature.account.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.account.service.AccountService
import kr.co.domain.feature.user.usecase.StartUserDataSyncUseCase
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import org.junit.jupiter.api.Test

class SignInUseCaseTest : DomainCoroutineTest() {

    private val accountService = mockk<AccountService>()
    private val startUserDataSync = mockk<StartUserDataSyncUseCase>()
    private val useCase = SignInUseCase(
        accountService = accountService,
        startUserDataSync = startUserDataSync,
    )

    @Test
    fun `returns account when sign in and user data sync succeed`() {
        runDomainTest {
            val account = DomainFixtures.account()
            coEvery { accountService.signIn(DomainFixtures.EMAIL, "value-test") } returns Ok(account)
            coEvery { startUserDataSync(account.uid) } returns Ok(Unit)

            val result = useCase(DomainFixtures.EMAIL, "value-test")

            result.assertOk(account)
            coVerify(exactly = 1) { accountService.signIn(DomainFixtures.EMAIL, "value-test") }
            coVerify(exactly = 1) { startUserDataSync(account.uid) }
            coVerify(exactly = 0) { accountService.signOut() }
        }
    }

    @Test
    fun `does not start user data sync when sign in fails`() {
        runDomainTest {
            val error = DomainError.Auth.InvalidCredentials
            coEvery { accountService.signIn(DomainFixtures.EMAIL, "value-test") } returns Err(error)

            val result = useCase(DomainFixtures.EMAIL, "value-test")

            result.assertErr(error)
            coVerify(exactly = 1) { accountService.signIn(DomainFixtures.EMAIL, "value-test") }
            coVerify(exactly = 0) { startUserDataSync(any()) }
            coVerify(exactly = 0) { accountService.signOut() }
        }
    }

    @Test
    fun `signs out as compensation when user data sync fails`() {
        runDomainTest {
            val account = DomainFixtures.account()
            val error = DomainError.NetworkUnavailable
            coEvery { accountService.signIn(DomainFixtures.EMAIL, "value-test") } returns Ok(account)
            coEvery { startUserDataSync(account.uid) } returns Err(error)
            coEvery { accountService.signOut() } returns Ok(Unit)

            val result = useCase(DomainFixtures.EMAIL, "value-test")

            result.assertErr(error)
            coVerify(exactly = 1) { accountService.signIn(DomainFixtures.EMAIL, "value-test") }
            coVerify(exactly = 1) { startUserDataSync(account.uid) }
            coVerify(exactly = 1) { accountService.signOut() }
        }
    }
}
