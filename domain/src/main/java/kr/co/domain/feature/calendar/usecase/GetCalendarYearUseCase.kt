package kr.co.domain.feature.calendar.usecase

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.calendar.model.CalendarMonth
import kr.co.domain.feature.calendar.repository.CalendarRepository
import java.time.Year
import javax.inject.Inject


class GetCalendarYearUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
) {
    operator fun invoke(targetYear: Year): Flow<PagingData<CalendarMonth>> {
        return calendarRepository.getYearlyPages(targetYear)
    }
}