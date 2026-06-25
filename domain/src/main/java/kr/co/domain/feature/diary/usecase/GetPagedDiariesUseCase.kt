package kr.co.domain.feature.diary.usecase

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError
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
    ): Result<List<Diary>, DomainError> {
        return diaryRepository.getPagedDiaries(query, startDate, endDate, limit, offset)
    }
}