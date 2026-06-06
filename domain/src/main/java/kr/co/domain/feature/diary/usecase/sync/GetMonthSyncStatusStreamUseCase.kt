package kr.co.domain.feature.diary.usecase.sync

import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.diary.model.SyncStatus
import kr.co.domain.feature.diary.repository.DiaryRepository
import java.time.YearMonth
import javax.inject.Inject

class GetMonthSyncStatusStreamUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<SyncStatus> =
        diaryRepository.getSyncStatusStream(yearMonth)
}