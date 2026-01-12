package kr.co.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kr.co.domain.model.calendar.yearmonth.MonthData
import kr.co.domain.model.calendar.yearmonth.YearMonthData
import java.time.Year
import java.time.YearMonth


interface CalendarRepository {
    fun getYearlyPages(targetYear: Year): Flow<PagingData<YearMonthData>>
    fun getMonthlyPages(targetYearMonth: YearMonth): Flow<PagingData<MonthData>>
}