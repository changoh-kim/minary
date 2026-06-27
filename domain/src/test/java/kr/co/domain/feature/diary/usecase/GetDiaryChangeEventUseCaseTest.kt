package kr.co.domain.feature.diary.usecase

import kr.co.domain.testing.fake.FakeDiaryRepository
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GetDiaryChangeEventUseCaseTest {

    @Test
    fun `returns diary change event flow`() {
        val repository = FakeDiaryRepository()
        val useCase = GetDiaryChangeEventUseCase(repository)

        val result = useCase()

        assertSame(repository.diaryChangeEvent, result)
    }
}
