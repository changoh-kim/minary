package kr.co.domain.feature.diary.usecase

import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject

class GetDiaryStreamUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
) {
    operator fun invoke(date: LocalDate): Flow<Diary?> {
        return diaryRepository.getDiaryStream(date)
    }
}
