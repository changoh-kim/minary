package kr.co.domain.feature.diary.usecase

import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.diary.repository.DiaryRepository
import javax.inject.Inject

class GetDiaryChangeEventUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
) {
    operator fun invoke(): Flow<Unit> {
        return diaryRepository.diaryChangeEvent
    }
}