package kr.co.domain.feature.diary.usecase

import com.github.michaelbull.result.Result
import kr.co.domain.error.DomainError
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject

class GetDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
) {
    suspend operator fun invoke(date: LocalDate): Result<Diary?, DomainError> {
        return diaryRepository.getDiary(date)
    }
}