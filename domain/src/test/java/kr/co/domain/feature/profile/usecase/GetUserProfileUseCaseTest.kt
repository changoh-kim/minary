package kr.co.domain.feature.profile.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kr.co.core.common.error.DomainError
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeUserProfileRepository
import org.junit.jupiter.api.Test

class GetUserProfileUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `returns first profile stream value`() {
        runDomainTest {
            val profile = DomainFixtures.userProfile()
            val repository = FakeUserProfileRepository(initialProfile = Ok(profile))
            val useCase = GetUserProfileUseCase(repository)

            val result = useCase()

            result.assertOk(profile)
        }
    }

    @Test
    fun `returns first profile stream failure`() {
        runDomainTest {
            val error = DomainError.Store.NotFound
            val repository = FakeUserProfileRepository(initialProfile = Err(error))
            val useCase = GetUserProfileUseCase(repository)

            val result = useCase()

            result.assertErr(error)
        }
    }
}
