package kr.co.data.feature.calendar.source.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kr.co.data.feature.calendar.mapper.CalendarDataMapper.toCalendarMonthDto
import kr.co.data.feature.calendar.model.CalendarMonthDto
import kr.co.domain.feature.calendar.generator.CalendarGenerator
import java.time.YearMonth


class CalendarMonthPagingSource(
    private val calendarGenerator: CalendarGenerator,
) : PagingSource<YearMonth, CalendarMonthDto>() {

    override fun getRefreshKey(state: PagingState<YearMonth, CalendarMonthDto>): YearMonth? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plusMonths(1) ?: anchorPage?.nextKey?.minusMonths(1)
        }
    }

    override suspend fun load(params: LoadParams<YearMonth>): LoadResult<YearMonth, CalendarMonthDto> {
        return try {
            val startYearMonth =
                YearMonth.of(CalendarGenerator.START_YEAR_1902, CalendarGenerator.START_MONTH_1)
            val currentYearMonth = YearMonth.now()
            val targetYearMonth = params.key ?: currentYearMonth
            val loadSize = params.loadSize

            val calendarMonthList = ArrayList<CalendarMonthDto>(loadSize)

            var firstMonth: YearMonth? = null
            var lastMonth: YearMonth? = null

            for (i in 0 until loadSize) {
                val yearMonth = targetYearMonth.minusMonths(i.toLong())

                if (yearMonth.isBefore(startYearMonth)) break
                if (yearMonth.isAfter(currentYearMonth)) continue

                if (firstMonth == null) firstMonth = yearMonth
                lastMonth = yearMonth

                calendarMonthList.add(
                    calendarGenerator.generateMonth(yearMonth).toCalendarMonthDto()
                )
            }

            if (calendarMonthList.isEmpty()) {
                return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            }

            val prevKey =
                if (firstMonth == null || firstMonth >= currentYearMonth) null
                else targetYearMonth.plusMonths(loadSize.toLong())

            val nextKey =
                if (lastMonth == null || lastMonth <= startYearMonth) null
                else targetYearMonth.minusMonths(loadSize.toLong())

            LoadResult.Page(
                data = calendarMonthList,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}