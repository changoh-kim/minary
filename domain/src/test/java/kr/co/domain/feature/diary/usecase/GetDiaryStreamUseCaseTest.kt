package kr.co.domain.feature.diary.usecase

import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.fake.FakeDiaryRepository
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GetDiaryStreamUseCaseTest {

    @Test
    fun `returns diary stream for requested date`() {
        val repository = FakeDiaryRepository(listOf(DomainFixtures.diary()))
        val useCase = GetDiaryStreamUseCase(repository)
        val expected = repository.getDiaryStream(DomainFixtures.DATE)

        val result = useCase(DomainFixtures.DATE)

        assertSame(expected, result)
    }
}
