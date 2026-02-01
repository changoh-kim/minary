package kr.co.domain.feature.diary.usecase

import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import javax.inject.Inject


class DeleteDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
) {
    suspend operator fun invoke(diary: Diary): Result<Unit> {
        return diaryRepository.deleteDiary(diary)
    }
}