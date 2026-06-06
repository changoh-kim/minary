package kr.co.domain.feature.diary.usecase

import com.github.michaelbull.result.Result
import kr.co.domain.error.DomainError
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.model.DiarySyncStatus
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.time.service.ServerTimeProvider
import javax.inject.Inject

class DeleteDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val serverTime: ServerTimeProvider,
) {
    suspend operator fun invoke(diary: Diary): Result<Unit, DomainError> {
        return diaryRepository.deleteDiary(
            diary.copy(
                updatedAt = serverTime.now(),
                syncStatus = DiarySyncStatus.PENDING_DELETE,
            )
        )
    }
}