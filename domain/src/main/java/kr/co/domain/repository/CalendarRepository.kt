package kr.co.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kr.co.domain.model.calendar.MonthData
import kr.co.domain.model.calendar.CalendarItem
import java.time.YearMonth


interface CalendarRepository {
    fun getYearlyPages(targetYear: Int): Flow<PagingData<CalendarItem>>
    fun getMonthlyPages(targetYearMonth: YearMonth): Flow<PagingData<MonthData>>
}