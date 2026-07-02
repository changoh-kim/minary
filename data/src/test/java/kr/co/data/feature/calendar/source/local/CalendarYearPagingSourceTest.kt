package kr.co.data.feature.calendar.source.local

import androidx.paging.PagingSource
import kr.co.data.feature.calendar.model.CalendarMonthModel
import kr.co.data.testing.BaseDataUnitTest
import kr.co.domain.feature.calendar.generator.CalendarGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Year

class CalendarYearPagingSourceTest : BaseDataUnitTest() {
    @Test
    fun `load returns ascending months for loaded year range`() = runDataTest {
        val targetYear = Year.now().minusYears(2)
        val source = CalendarYearPagingSource(CalendarGenerator())

        val page = source.load(
            PagingSource.LoadParams.Refresh(
                key = targetYear,
                loadSize = 2,
                placeholdersEnabled = false,
            )
        ).assertPage()

        assertEquals(24, page.data.size)
        assertEquals(targetYear.minusYears(1).atMonth(1), page.data.first().yearMonth)
        assertEquals(targetYear.atMonth(12), page.data.last().yearMonth)
        assertEquals(targetYear.minusYears(2), page.prevKey)
        assertEquals(targetYear.plusYears(2), page.nextKey)
    }

    @Test
    fun `load stops at start year and has no previous key`() = runDataTest {
        val source = CalendarYearPagingSource(CalendarGenerator())

        val page = source.load(
            PagingSource.LoadParams.Refresh(
                key = Year.of(CalendarGenerator.START_YEAR_1902),
                loadSize = 3,
                placeholdersEnabled = false,
            )
        ).assertPage()

        assertEquals(12, page.data.size)
        assertEquals(Year.of(CalendarGenerator.START_YEAR_1902).atMonth(1), page.data.first().yearMonth)
        assertEquals(Year.of(CalendarGenerator.START_YEAR_1902).atMonth(12), page.data.last().yearMonth)
        assertNull(page.prevKey)
        assertEquals(Year.of(CalendarGenerator.START_YEAR_1902 + 3), page.nextKey)
    }

    private fun PagingSource.LoadResult<Year, CalendarMonthModel>.assertPage():
        PagingSource.LoadResult.Page<Year, CalendarMonthModel> =
        this as? PagingSource.LoadResult.Page
            ?: throw AssertionError("Expected Page but was $this")
}
