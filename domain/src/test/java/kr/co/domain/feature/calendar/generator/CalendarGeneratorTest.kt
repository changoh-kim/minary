package kr.co.domain.feature.calendar.generator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

class CalendarGeneratorTest {

    private val generator = CalendarGenerator()

    @Test
    fun `generates forty two days and marks current month days`() {
        val month = generator.generateMonth(YearMonth.of(2026, 1))

        assertEquals(CalendarGenerator.CALENDAR_GRID_DAY_COUNT, month.days.size)
        assertEquals(31, month.days.count { it.isCurrentMonth })
        assertEquals(LocalDate.of(2026, 1, 1), month.days.first { it.isCurrentMonth }.date)
        assertEquals(LocalDate.of(2026, 1, 31), month.days.last { it.isCurrentMonth }.date)
        assertFalse(month.days.first().isCurrentMonth)
    }

    @Test
    fun `starts with current month when first day is sunday`() {
        val month = generator.generateMonth(YearMonth.of(2023, 10))

        assertEquals(LocalDate.of(2023, 10, 1), month.days.first().date)
        assertTrue(month.days.first().isCurrentMonth)
        assertEquals(CalendarGenerator.CALENDAR_GRID_DAY_COUNT, month.days.size)
    }

    @Test
    fun `generates leap year february with twenty nine current days`() {
        val month = generator.generateMonth(YearMonth.of(2024, 2))

        assertEquals(29, month.days.count { it.isCurrentMonth })
        assertTrue(month.days.any { it.date == LocalDate.of(2024, 2, 29) && it.isCurrentMonth })
    }

    @Test
    fun `generates full year months in ascending order`() {
        val months = generator.generateMonths(Year.of(2026), CalendarGenerator.SortOrder.Ascending)

        assertEquals(CalendarGenerator.MONTHS_IN_YEAR, months.size)
        assertEquals(YearMonth.of(2026, 1), months.first().yearMonth)
        assertEquals(YearMonth.of(2026, 12), months.last().yearMonth)
    }

    @Test
    fun `generates full year months in descending order`() {
        val months = generator.generateMonths(Year.of(2026), CalendarGenerator.SortOrder.Descending)

        assertEquals(CalendarGenerator.MONTHS_IN_YEAR, months.size)
        assertEquals(YearMonth.of(2026, 12), months.first().yearMonth)
        assertEquals(YearMonth.of(2026, 1), months.last().yearMonth)
    }

    @Test
    fun `generates relative month window in requested size and order`() {
        val target = YearMonth.of(2026, 6)

        val ascending = generator.generateMonths(
            yearMonth = target,
            size = 3,
            sortOrder = CalendarGenerator.SortOrder.Ascending,
        )
        val descending = generator.generateMonths(
            yearMonth = target,
            size = 3,
            sortOrder = CalendarGenerator.SortOrder.Descending,
        )

        assertEquals(
            listOf(
                YearMonth.of(2026, 4),
                YearMonth.of(2026, 5),
                YearMonth.of(2026, 6),
            ),
            ascending.map { it.yearMonth },
        )
        assertEquals(
            listOf(
                YearMonth.of(2026, 6),
                YearMonth.of(2026, 5),
                YearMonth.of(2026, 4),
            ),
            descending.map { it.yearMonth },
        )
    }
}
