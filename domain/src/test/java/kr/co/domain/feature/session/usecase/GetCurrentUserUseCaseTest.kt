package kr.co.domain.feature.session.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kr.co.core.common.error.DomainError
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeSessionRepository
import org.junit.jupiter.api.Test

class GetCurrentUserUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `returns current user from repository`() {
        runDomainTest {
            val session = DomainFixtures.userSession()
            val repository = FakeSessionRepository(currentUserResult = Ok(session))
            val useCase = GetCurrentUserUseCase(repository)

            val result = useCase()

            result.assertOk(session)
        }
    }

    @Test
    fun `returns repository failure`() {
        runDomainTest {
            val error = DomainError.Auth.UserNotFound
            val repository = FakeSessionRepository(currentUserResult = Err(error))
            val useCase = GetCurrentUserUseCase(repository)

            val result = useCase()

            result.assertErr(error)
        }
    }
}
