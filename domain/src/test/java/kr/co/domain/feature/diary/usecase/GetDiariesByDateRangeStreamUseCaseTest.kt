package kr.co.domain.feature.diary.usecase

import kotlinx.coroutines.flow.first
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.fake.FakeDiaryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class GetDiariesByDateRangeStreamUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `uses previous current and next month as default date range`() {
        runDomainTest {
            val diary = DomainFixtures.diary(date = LocalDate.of(2026, 2, 14))
            val diaryRepository = FakeDiaryRepository(initialDiaries = listOf(diary))
            val useCase = GetDiariesByDateRangeStreamUseCase(diaryRepository)

            val result = useCase(YearMonth.of(2026, 2)).first()

            assertEquals(listOf(diary), result)
            assertEquals(
                listOf(LocalDate.of(2026, 1, 1) to LocalDate.of(2026, 3, 31)),
                diaryRepository.requestedDateRanges,
            )
        }
    }

    @Test
    fun `uses custom month range around center month`() {
        runDomainTest {
            val diaryRepository = FakeDiaryRepository()
            val useCase = GetDiariesByDateRangeStreamUseCase(diaryRepository)

            val result = useCase(
                centerMonth = YearMonth.of(2026, 2),
                monthRange = 2L,
            ).first()

            assertEquals(emptyList<Diary>(), result)
            assertEquals(
                listOf(LocalDate.of(2025, 12, 1) to LocalDate.of(2026, 4, 30)),
                diaryRepository.requestedDateRanges,
            )
        }
    }
}
