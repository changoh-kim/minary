package kr.co.domain.feature.diary.usecase

import kr.co.domain.feature.diary.repository.DiaryRepository
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