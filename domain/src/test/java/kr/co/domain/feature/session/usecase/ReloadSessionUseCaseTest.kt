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

class ReloadSessionUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `returns reloaded session from repository`() {
        runDomainTest {
            val session = DomainFixtures.userSession()
            val repository = FakeSessionRepository(reloadResult = Ok(session))
            val useCase = ReloadSessionUseCase(repository)

            val result = useCase()

            result.assertOk(session)
        }
    }

    @Test
    fun `returns repository failure`() {
        runDomainTest {
            val error = DomainError.Auth.TokenExpired
            val repository = FakeSessionRepository(reloadResult = Err(error))
            val useCase = ReloadSessionUseCase(repository)

            val result = useCase()

            result.assertErr(error)
        }
    }
}
