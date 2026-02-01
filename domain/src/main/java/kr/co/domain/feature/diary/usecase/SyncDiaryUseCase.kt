package kr.co.domain.feature.diary.usecase

import kr.co.domain.feature.diary.repository.DiaryRepository
import javax.inject.Inject


class SyncDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
){
    suspend operator fun invoke() {
        diaryRepository.scheduleDiarySync()
    }
}