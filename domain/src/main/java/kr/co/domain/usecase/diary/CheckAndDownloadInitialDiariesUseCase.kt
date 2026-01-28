package kr.co.domain.usecase.diary

import kr.co.domain.repository.DiaryRepository
import javax.inject.Inject


class CheckAndDownloadInitialDiariesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke() {
        if (!diaryRepository.hasDiaries()) {
            diaryRepository.scheduleDiaryDownload()
        }
    }
}