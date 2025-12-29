package kr.co.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kr.co.data.START_YEAR_1902
import kr.co.domain.model.calendar.CalendarItem
import kr.co.domain.model.calendar.MonthData
import kr.co.domain.model.calendar.YearData
import java.time.Year


class CalendarYearPagingSource : PagingSource<Int, CalendarItem>() {

    override fun getRefreshKey(state: PagingState<Int, CalendarItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CalendarItem> {
        return try {
            val startYear = START_YEAR_1902
            val currentYear = Year.now().value
            val targetYear = params.key ?: currentYear
            val loadSize = params.loadSize

            val expectedSize = loadSize * 13
            val itemList = ArrayList<CalendarItem>(expectedSize)

            var firstYear: Int? = null
            var lastYear: Int? = null

            for (i in 0 until loadSize) {
                val year = targetYear - i

                if (year < startYear) break
                if (year > currentYear) continue

                if (firstYear == null) firstYear = year
                lastYear = year

                itemList.addAll(createCalendarItems(year))
            }

            val prevKey =
                if (firstYear == null || firstYear >= currentYear) null
                else targetYear + loadSize

            val nextKey =
                if (lastYear == null || lastYear <= startYear) null
                else targetYear - loadSize

            LoadResult.Page(
                data = itemList,
                prevKey = prevKey,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /*
     * Grid에 표시 하기 위한 출력 순서에 맞춰 생성
     * -> 10/11/12, 7/8/9, 4/5/6, 1/2/3
     */
    private fun createCalendarItems(year: Int): List<CalendarItem> {
        val result = ArrayList<CalendarItem>(13)

        val quarterStartMonths = intArrayOf(10, 7, 4, 1)

        for (quarterStartMonth in quarterStartMonths) {
            for (i in 0..2) {
                val month = quarterStartMonth + i

                result.add(
                    MonthData(
                        year = year,
                        month = month,
                    )
                )
            }
        }

        result.add(YearData(year = year))

        return result
    }
}
