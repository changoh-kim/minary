package kr.co.domain.feature.diary.usecase.setting

import kr.co.domain.testing.fake.FakeUserSettingsRepository
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GetDiarySettingsStreamUseCaseTest {

    @Test
    fun `returns diary sync enabled stream`() {
        val repository = FakeUserSettingsRepository(initialDiarySyncEnabled = true)
        val useCase = GetDiarySettingsStreamUseCase(repository)

        val result = useCase()

        assertSame(repository.diarySyncEnabledStream, result)
    }
}
