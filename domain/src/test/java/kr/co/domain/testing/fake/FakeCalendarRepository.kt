package kr.co.domain.testing.fake

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kr.co.domain.feature.calendar.model.CalendarMonth
import kr.co.domain.feature.calendar.repository.CalendarRepository
import java.time.Year
import java.time.YearMonth

class FakeCalendarRepository : CalendarRepository {
    val yearlyPagesFlow = MutableStateFlow<PagingData<CalendarMonth>>(PagingData.empty())
    val monthlyPagesFlow = MutableStateFlow<PagingData<CalendarMonth>>(PagingData.empty())

    val requestedYears = mutableListOf<Year>()
    val requestedYearMonths = mutableListOf<YearMonth>()

    override fun getYearlyPages(targetYear: Year): Flow<PagingData<CalendarMonth>> {
        requestedYears += targetYear
        return yearlyPagesFlow
    }

    override fun getMonthlyPages(targetYearMonth: YearMonth): Flow<PagingData<CalendarMonth>> {
        requestedYearMonths += targetYearMonth
        return monthlyPagesFlow
    }
}
