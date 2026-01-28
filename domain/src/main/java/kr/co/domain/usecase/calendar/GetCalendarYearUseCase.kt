package kr.co.domain.usecase.calendar

import kr.co.domain.repository.CalendarRepository
import java.time.Year
import javax.inject.Inject


class GetCalendarYearUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
) {
    operator fun invoke(targetYear: Year) = calendarRepository.getYearlyPages(targetYear)
}