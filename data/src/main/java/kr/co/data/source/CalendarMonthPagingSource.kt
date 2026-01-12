package kr.co.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kr.co.data.START_MONTH_1
import kr.co.data.START_YEAR_1902
import kr.co.data.model.calendar.yearmonth.MonthDTO
import java.time.YearMonth


class CalendarMonthPagingSource(
    private val calendarLocalDataSource: CalendarLocalDataSource
) : PagingSource<YearMonth, MonthDTO>() {

    override fun getRefreshKey(state: PagingState<YearMonth, MonthDTO>): YearMonth? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plusMonths(1) ?: anchorPage?.nextKey?.minusMonths(1)
        }
    }

    override suspend fun load(params: LoadParams<YearMonth>): LoadResult<YearMonth, MonthDTO> {
        return try {
            val startYearMonth = YearMonth.of(START_YEAR_1902, START_MONTH_1)
            val currentYearMonth = YearMonth.now()
            val targetYearMonth = params.key ?: currentYearMonth
            val loadSize = params.loadSize

            val monthList = ArrayList<MonthDTO>(loadSize)

            var firstMonth: YearMonth? = null
            var lastMonth: YearMonth? = null

            for (i in 0 until loadSize) {
                val yearMonth = targetYearMonth.minusMonths(i.toLong())

                if (yearMonth.isBefore(startYearMonth)) break
                if (yearMonth.isAfter(currentYearMonth)) continue

                if (firstMonth == null) firstMonth = yearMonth
                lastMonth = yearMonth

                monthList.add(calendarLocalDataSource.createMonth(yearMonth))
            }

            if (monthList.isEmpty()) {
                return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            }

            val prevKey =
                if (firstMonth == null || firstMonth >= currentYearMonth) null
                else targetYearMonth.plusMonths(loadSize.toLong())

            val nextKey =
                if (lastMonth == null || lastMonth <= startYearMonth) null
                else targetYearMonth.minusMonths(loadSize.toLong())

            LoadResult.Page(
                data = monthList,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
