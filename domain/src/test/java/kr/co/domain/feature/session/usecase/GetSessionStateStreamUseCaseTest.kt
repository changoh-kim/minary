package kr.co.domain.feature.session.usecase

import kr.co.domain.testing.fake.FakeSessionRepository
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GetSessionStateStreamUseCaseTest {

    @Test
    fun `returns session state flow`() {
        val repository = FakeSessionRepository()
        val useCase = GetSessionStateStreamUseCase(repository)

        val result = useCase()

        assertSame(repository.sessionState, result)
    }
}
