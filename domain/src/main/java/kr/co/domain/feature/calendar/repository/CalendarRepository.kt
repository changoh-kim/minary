package kr.co.domain.feature.calendar.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.calendar.model.CalendarMonth
import java.time.Year
import java.time.YearMonth


interface CalendarRepository {
    fun getYearlyPages(targetYear: Year): Flow<PagingData<CalendarMonth>>
    fun getMonthlyPages(targetYearMonth: YearMonth): Flow<PagingData<CalendarMonth>>
}