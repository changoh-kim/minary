package kr.co.data.feature.calendar.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.data.feature.calendar.mapper.CalendarMonthMapper.toCalendarMonth
import kr.co.data.feature.calendar.source.local.CalendarMonthPagingSource
import kr.co.data.feature.calendar.source.local.CalendarYearPagingSource
import kr.co.domain.feature.calendar.model.CalendarMonth
import kr.co.domain.feature.calendar.repository.CalendarRepository
import kr.co.domain.feature.calendar.service.CalendarGenerator
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.session.repository.SessionRepository
import java.time.Year
import java.time.YearMonth
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val calendarGenerator: CalendarGenerator,
    private val sessionRepository: SessionRepository,
    private val diaryRepository: DiaryRepository,
) : CalendarRepository {
    override fun getYearlyPages(targetYear: Year): Flow<PagingData<CalendarMonth>> {
        return Pager(
            config = PagingConfig(
                /**
                 * 초기 12년
                 * 이후 12년
                 * 끝에서 6번째 칸에 도달하면 다음 데이터 요청
                 * 비어있는 공간은 없도록 처리
                 * 최대 432개(36년) 월 목록을 가짐 -> [ 12년 * 12개월 * 3배 ]
                 */
                initialLoadSize = 12,
                pageSize = 12,
                prefetchDistance = 6,
                enablePlaceholders = false,
                maxSize = 432
            ),
            initialKey = targetYear,
            pagingSourceFactory = {
                CalendarYearPagingSource(calendarGenerator)
            }
        ).flow.map { pagingData ->
            pagingData.map { calendarMonthDto -> calendarMonthDto.toCalendarMonth() }
        }
    }

    override fun getMonthlyPages(targetYearMonth: YearMonth): Flow<PagingData<CalendarMonth>> {
        return Pager(
            config = PagingConfig(
                /**
                 * 초기 12개월
                 * 이후 12개월
                 * 끝에서 4번째 칸에 도달하면 다음 데이터 요청
                 * 비어있는 공간은 없도록 처리
                 * 최대 36개(3년) 월 목록을 가짐 -> [ 12개월 * 3배 ]
                 */
                initialLoadSize = 12,
                pageSize = 12,
                prefetchDistance = 4,
                enablePlaceholders = false,
                maxSize = 36
            ),
            initialKey = targetYearMonth,
            pagingSourceFactory = {
                CalendarMonthPagingSource(
                    calendarGenerator,
                    sessionRepository,
                    diaryRepository
                )
            }
        ).flow.map { pagingData ->
            pagingData.map { calendarMonthDto -> calendarMonthDto.toCalendarMonth() }
        }
    }
}