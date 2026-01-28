package kr.co.domain.usecase.diary

import kotlinx.coroutines.flow.Flow
import kr.co.domain.model.diary.DiaryData
import kr.co.domain.repository.DiaryRepository
import java.time.YearMonth
import javax.inject.Inject


class GetDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(centerMonth: YearMonth, monthRange: Long = 1L): Flow<List<DiaryData>> {
        val startDate = centerMonth.minusMonths(monthRange).atDay(1)
        val endDate = centerMonth.plusMonths(monthRange).atEndOfMonth()
        return diaryRepository.getDiariesFlow(startDate, endDate)
    }
}