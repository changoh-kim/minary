package kr.co.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.data.mapper.CalendarDataMapper.toMonthData
import kr.co.data.mapper.CalendarDataMapper.toYearMonthData
import kr.co.data.source.CalendarLocalDataSource
import kr.co.data.source.CalendarMonthPagingSource
import kr.co.data.source.CalendarYearPagingSource
import kr.co.domain.model.calendar.yearmonth.MonthData
import kr.co.domain.model.calendar.yearmonth.YearMonthData
import kr.co.domain.repository.CalendarRepository
import java.time.Year
import java.time.YearMonth
import javax.inject.Inject


class CalendarRepositoryImpl @Inject constructor(
    private val calendarLocalDataSource: CalendarLocalDataSource,
) : CalendarRepository {

    override fun getYearlyPages(targetYear: Year): Flow<PagingData<YearMonthData>> {
        return Pager(
            config = PagingConfig(
                /**
                 * 초기 9년
                 * 이후 9년
                 * 끝에서 4번째 칸에 도달하면 다음 데이터 요청
                 * 비어있는 공간은 없도록 처리
                 * 최대 351개의 목록을 가짐 -> 27년치 [ 9년 x 13개{ header(year)1개 + item(month)12개 } x 3배 ]
                 */
                initialLoadSize = 9,
                pageSize = 9,
                prefetchDistance = 4,
                enablePlaceholders = false,
                maxSize = 351
            ),
            initialKey = targetYear,
            pagingSourceFactory = {
                CalendarYearPagingSource(calendarLocalDataSource)
            }
        ).flow.map { pagingData ->
            pagingData.map { yearMonthDTO ->
                yearMonthDTO.toYearMonthData()
            }
        }
    }

    override fun getMonthlyPages(targetYearMonth: YearMonth): Flow<PagingData<MonthData>> {
        return Pager(
            config = PagingConfig(
                /**
                 * 초기 12개월
                 * 이후 12개월
                 * 끝에서 4번째 칸에 도달하면 다음 데이터 요청
                 * 비어있는 공간은 없도록 처리
                 * 최대 72개월의 목록을 가짐 -> 6년치
                 */
                initialLoadSize = 12,
                pageSize = 12,
                prefetchDistance = 4,
                enablePlaceholders = false,
                maxSize = 72
            ),
            initialKey = targetYearMonth,
            pagingSourceFactory = {
                CalendarMonthPagingSource(calendarLocalDataSource)
            }
        ).flow.map { pagingData ->
            pagingData.map { monthDTO ->
                monthDTO.toMonthData()
            }
        }
    }
}
