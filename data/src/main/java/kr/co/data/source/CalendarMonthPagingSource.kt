package kr.co.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kr.co.data.START_MONTH_1
import kr.co.data.START_YEAR_1902
import kr.co.domain.model.calendar.MonthData
import java.time.YearMonth


class CalendarMonthPagingSource : PagingSource<YearMonth, MonthData>() {

    override fun getRefreshKey(state: PagingState<YearMonth, MonthData>): YearMonth? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plusMonths(1) ?: anchorPage?.nextKey?.minusMonths(1)
        }
    }

    override suspend fun load(params: LoadParams<YearMonth>): LoadResult<YearMonth, MonthData> {
        return try {
            val startYearMonth = YearMonth.of(START_YEAR_1902, START_MONTH_1)
            val currentYearMonth = YearMonth.now()
            val targetYearMonth = params.key ?: currentYearMonth
            val loadSize = params.loadSize

            val monthDataList = ArrayList<MonthData>(loadSize)

            var firstLoadedMonth: YearMonth? = null
            var lastLoadedMonth: YearMonth? = null

            for (index in 0 until loadSize) {
                val yearMonth = targetYearMonth.minusMonths(index.toLong())

                if (yearMonth.isBefore(startYearMonth)) break
                if (yearMonth.isAfter(currentYearMonth)) continue

                if (firstLoadedMonth == null) firstLoadedMonth = yearMonth
                lastLoadedMonth = yearMonth

                monthDataList.add(
                    MonthData(
                        year = yearMonth.year,
                        month = yearMonth.monthValue,
                    )
                )
            }

            if (monthDataList.isEmpty()) {
                return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            }

            val prevKey =
                if (firstLoadedMonth == null || firstLoadedMonth >= currentYearMonth) null
                else targetYearMonth.plusMonths(loadSize.toLong())

            val nextKey =
                if (lastLoadedMonth == null || lastLoadedMonth <= startYearMonth) null
                else targetYearMonth.minusMonths(loadSize.toLong())

            LoadResult.Page(
                data = monthDataList,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}