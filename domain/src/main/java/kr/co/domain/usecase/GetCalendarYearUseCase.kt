package kr.co.domain.usecase

import kr.co.domain.repository.CalendarRepository
import javax.inject.Inject


class GetCalendarYearUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
) {
    operator fun invoke(targetYear: Int) = calendarRepository.getYearlyPages(targetYear)
}