package kr.co.domain.feature.calendar.usecase

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.calendar.model.CalendarMonth
import kr.co.domain.feature.calendar.repository.CalendarRepository
import java.time.YearMonth
import javax.inject.Inject


class GetCalendarMonthUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
) {
    operator fun invoke(targetYearMonth: YearMonth): Flow<PagingData<CalendarMonth>> {
        return calendarRepository.getMonthlyPages(targetYearMonth)
    }
}