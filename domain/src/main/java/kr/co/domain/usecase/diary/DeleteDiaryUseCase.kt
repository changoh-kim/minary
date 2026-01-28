package kr.co.domain.usecase.diary

import kr.co.domain.model.diary.DiaryData
import kr.co.domain.repository.DiaryRepository
import javax.inject.Inject


class DeleteDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
) {
    suspend operator fun invoke(diaryData: DiaryData): Result<Unit> {
        return diaryRepository.deleteDiary(diaryData)
    }
}