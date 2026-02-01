package kr.co.data.feature.calendar.source.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kr.co.data.feature.calendar.mapper.CalendarDataMapper.toCalendarMonthDto
import kr.co.data.feature.calendar.model.CalendarMonthDto
import kr.co.domain.feature.calendar.generator.CalendarGenerator
import java.time.Year


class CalendarYearPagingSource(
    private val calendarGenerator: CalendarGenerator,
) : PagingSource<Year, CalendarMonthDto>() {

    override fun getRefreshKey(state: PagingState<Year, CalendarMonthDto>): Year? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plusYears(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minusYears(1)
        }
    }

    override suspend fun load(params: LoadParams<Year>): LoadResult<Year, CalendarMonthDto> {
        return try {
            val startYear = Year.of(CalendarGenerator.START_YEAR_1902)
            val currentYear = Year.now()
            val targetYear = params.key ?: currentYear
            val loadSize = params.loadSize

            val expectedSize = loadSize * 12
            val calendarMonthList = ArrayList<CalendarMonthDto>(expectedSize)

            var firstYear: Year? = null
            var lastYear: Year? = null

            val startRange = loadSize - 1
            for (i in startRange downTo 0) {
                val year = targetYear.minusYears(i.toLong())

                if (year.isBefore(startYear)) continue
                if (year.isAfter(currentYear)) break

                if (firstYear == null) firstYear = year
                lastYear = year

                calendarMonthList.addAll(
                    calendarGenerator.generateMonths(
                        year,
                        CalendarGenerator.SortOrder.Ascending,
                    ).map { it.toCalendarMonthDto() }
                )
            }

            if (calendarMonthList.isEmpty()) {
                return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            }

            val prevKey =
                if (firstYear == null || firstYear <= startYear) null
                else targetYear.minusYears(loadSize.toLong())

            val nextKey =
                if (lastYear == null || lastYear >= currentYear) null
                else targetYear.plusYears(loadSize.toLong())

            LoadResult.Page(
                data = calendarMonthList,
                prevKey = prevKey,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}