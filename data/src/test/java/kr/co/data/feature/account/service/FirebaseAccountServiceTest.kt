package kr.co.data.feature.account.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.co.core.common.error.DomainError
import kr.co.core.common.model.Gender
import kr.co.data.feature.account.model.AccountModel
import kr.co.data.feature.account.source.remote.AccountRemoteDataSource
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.assertErr
import kr.co.data.testing.assertOk
import kr.co.data.testing.fake.FakeAppLogger
import kr.co.domain.feature.account.model.SignUpInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FirebaseAccountServiceTest : BaseDataUnitTest() {
    private val logger = FakeAppLogger()
    private val remoteDataSource = mockk<AccountRemoteDataSource>()
    private val service = FirebaseAccountService(
        logger = logger,
        remoteDataSource = remoteDataSource,
    )

    @Test
    fun `createAccount forwards sign up fields and joinedAt to remote source`() = runDataTest {
        coEvery {
            remoteDataSource.createAccount(
                email = any(),
                password = any(),
                name = any(),
                gender = any(),
                birthday = any(),
                address = any(),
                phoneNumber = any(),
                joinedAt = any(),
            )
        } returns Unit

        service.createAccount(signUpInfo(), DataFixtures.JOINED_AT).assertOk(Unit)

        coVerify(exactly = 1) {
            remoteDataSource.createAccount(
                email = DataFixtures.EMAIL,
                password = PASSWORD,
                name = DataFixtures.NAME,
                gender = Gender.FEMALE.name,
                birthday = DataFixtures.birthday.toString(),
                address = DataFixtures.ADDRESS,
                phoneNumber = DataFixtures.PHONE_NUMBER,
                joinedAt = DataFixtures.JOINED_AT,
            )
        }
    }

    @Test
    fun `createAccount maps remote exception to DomainError and logs failure`() = runDataTest {
        coEvery {
            remoteDataSource.createAccount(any(), any(), any(), any(), any(), any(), any(), any())
        } throws IllegalStateException("failure-test")

        service.createAccount(signUpInfo(), DataFixtures.JOINED_AT).assertErr(DomainError.Unexpected)

        assertEquals("Failed to create account with profile", logger.errorThrowables.single().second)
    }

    @Test
    fun `signIn maps remote account model to domain account`() = runDataTest {
        coEvery { remoteDataSource.signIn(DataFixtures.EMAIL, PASSWORD) } returns AccountModel(
            uid = DataFixtures.UID,
            email = DataFixtures.EMAIL,
        )

        service.signIn(DataFixtures.EMAIL, PASSWORD).assertOk(DataFixtures.account)
    }

    @Test
    fun `signIn maps remote exception to DomainError and logs failure`() = runDataTest {
        coEvery { remoteDataSource.signIn(DataFixtures.EMAIL, PASSWORD) } throws IllegalStateException("failure-test")

        service.signIn(DataFixtures.EMAIL, PASSWORD).assertErr(DomainError.Unexpected)

        assertEquals("Failed to sign in", logger.errorThrowables.single().second)
    }

    @Test
    fun `deleteAccount returns deleted uid from remote source`() = runDataTest {
        coEvery { remoteDataSource.deleteAccount(PASSWORD) } returns DataFixtures.UID

        service.deleteAccount(PASSWORD).assertOk(DataFixtures.UID)
    }

    @Test
    fun `signOut delegates to remote source and wraps success`() = runDataTest {
        every { remoteDataSource.signOut() } returns Unit

        service.signOut().assertOk(Unit)

        verify(exactly = 1) { remoteDataSource.signOut() }
    }

    @Test
    fun `checkEmailAvailability returns remote boolean`() = runDataTest {
        coEvery { remoteDataSource.checkEmailAvailability(DataFixtures.EMAIL) } returns true

        service.checkEmailAvailability(DataFixtures.EMAIL).assertOk(true)
    }

    private fun signUpInfo() = SignUpInfo(
        email = DataFixtures.EMAIL,
        password = PASSWORD,
        name = DataFixtures.NAME,
        gender = Gender.FEMALE,
        birthday = DataFixtures.birthday,
        address = DataFixtures.ADDRESS,
        phoneNumber = DataFixtures.PHONE_NUMBER,
    )

    private companion object {
        const val PASSWORD = "password-test"
    }
}
