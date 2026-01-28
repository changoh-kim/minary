package kr.co.domain.usecase.diary

import kr.co.domain.repository.DiaryRepository
import javax.inject.Inject


class SyncDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
){
    suspend operator fun invoke() {
        diaryRepository.scheduleDiarySync()
    }
}