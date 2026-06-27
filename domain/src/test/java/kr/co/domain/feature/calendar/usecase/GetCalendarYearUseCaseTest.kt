package kr.co.domain.feature.calendar.usecase

import kr.co.domain.testing.fake.FakeCalendarRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.time.Year

class GetCalendarYearUseCaseTest {

    @Test
    fun `returns yearly pages flow and passes target year`() {
        val repository = FakeCalendarRepository()
        val useCase = GetCalendarYearUseCase(repository)
        val target = Year.of(2026)

        val result = useCase(target)

        assertSame(repository.yearlyPagesFlow, result)
        assertEquals(listOf(target), repository.requestedYears)
    }
}
