package kr.co.domain.feature.calendar.usecase

import kr.co.domain.testing.fake.FakeCalendarRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.time.YearMonth

class GetCalendarMonthUseCaseTest {

    @Test
    fun `returns monthly pages flow and passes target year month`() {
        val repository = FakeCalendarRepository()
        val useCase = GetCalendarMonthUseCase(repository)
        val target = YearMonth.of(2026, 6)

        val result = useCase(target)

        assertSame(repository.monthlyPagesFlow, result)
        assertEquals(listOf(target), repository.requestedYearMonths)
    }
}
