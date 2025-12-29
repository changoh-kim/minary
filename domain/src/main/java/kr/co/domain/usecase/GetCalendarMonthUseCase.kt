package kr.co.domain.usecase

import kr.co.domain.repository.CalendarRepository
import java.time.YearMonth
import javax.inject.Inject


class GetCalendarMonthUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
) {
    operator fun invoke(targetYearMonth: YearMonth) = calendarRepository.getMonthlyPages(targetYearMonth)
}