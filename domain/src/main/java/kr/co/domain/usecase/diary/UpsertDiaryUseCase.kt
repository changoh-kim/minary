package kr.co.domain.usecase.diary

import kr.co.domain.model.diary.DiaryData
import kr.co.domain.model.diary.UpsertResultDiaryData
import kr.co.domain.repository.DiaryRepository
import javax.inject.Inject


class UpsertDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
){
    suspend operator fun invoke(diaryData: DiaryData): Result<UpsertResultDiaryData> {
        return diaryRepository.upsertDiary(diaryData)
    }
}