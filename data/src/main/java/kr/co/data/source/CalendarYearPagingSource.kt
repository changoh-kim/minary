package kr.co.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kr.co.data.START_YEAR_1902
import kr.co.data.model.calendar.yearmonth.YearMonthDTO
import java.time.Year


class CalendarYearPagingSource(
    private val calendarLocalDataSource: CalendarLocalDataSource
) : PagingSource<Year, YearMonthDTO>() {

    override fun getRefreshKey(state: PagingState<Year, YearMonthDTO>): Year? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plusYears(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minusYears(1)
        }
    }

    override suspend fun load(params: LoadParams<Year>): LoadResult<Year, YearMonthDTO> {
        return try {
            val startYear = Year.of(START_YEAR_1902)
            val currentYear = Year.now()
            val targetYear = params.key ?: currentYear
            val loadSize = params.loadSize

            val expectedSize = loadSize * 13
            val gridItems = ArrayList<YearMonthDTO>(expectedSize)

            var firstYear: Year? = null
            var lastYear: Year? = null

            for (i in 0 until loadSize) {
                val year = targetYear.minusYears(i.toLong())

                if (year.isBefore(startYear)) break
                if (year.isAfter(currentYear)) continue

                if (firstYear == null) firstYear = year
                lastYear = year

                gridItems.addAll(calendarLocalDataSource.createYearMonths(year))
            }

            if (gridItems.isEmpty()) {
                return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            }

            val prevKey =
                if (firstYear == null || firstYear >= currentYear) null
                else targetYear.plusYears(loadSize.toLong())

            val nextKey =
                if (lastYear == null || lastYear <= startYear) null
                else targetYear.minusYears(loadSize.toLong())

            LoadResult.Page(
                data = gridItems,
                prevKey = prevKey,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
