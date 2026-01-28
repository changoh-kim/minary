package kr.co.domain.usecase.diary

import kr.co.domain.model.diary.DiaryData
import kr.co.domain.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject


class GetDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
) {
    suspend operator fun invoke(date: LocalDate): Result<DiaryData> {
        return diaryRepository.getDiary(date)
    }
}