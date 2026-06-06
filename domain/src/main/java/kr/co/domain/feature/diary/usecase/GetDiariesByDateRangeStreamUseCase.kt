package kr.co.domain.feature.diary.usecase

import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import java.time.YearMonth
import javax.inject.Inject


class GetDiariesByDateRangeStreamUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(centerMonth: YearMonth, monthRange: Long = 1L): Flow<List<Diary>> {
        val startDate = centerMonth.minusMonths(monthRange).atDay(1)
        val endDate = centerMonth.plusMonths(monthRange).atEndOfMonth()
        return diaryRepository.getDiariesByDateRangeStream(startDate, endDate)
    }
}