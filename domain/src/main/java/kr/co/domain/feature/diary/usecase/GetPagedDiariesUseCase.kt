package kr.co.domain.feature.diary.usecase

import kr.co.core.common.result.AppResult
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject

class GetPagedDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(
        query: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        limit: Int,
        offset: Int
    ): AppResult<List<Diary>> {
        return diaryRepository.getPagedDiaries(query, startDate, endDate, limit, offset)
    }
}