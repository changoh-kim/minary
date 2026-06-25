package kr.co.domain.feature.diary.repository

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.diary.model.Diary
import kr.co.core.common.state.SyncStatus
import java.time.LocalDate
import java.time.YearMonth


interface DiaryRepository {
    fun getSyncStatusStream(yearMonth: YearMonth): Flow<SyncStatus>
    suspend fun requestMonthSync(userId: String, yearMonth: YearMonth): Result<Unit, DomainError>

    suspend fun createDiary(diary: Diary): Result<Unit, DomainError>
    suspend fun updateDiary(diary: Diary): Result<Diary, DomainError>
    suspend fun deleteDiary(diary: Diary): Result<Unit, DomainError>
    suspend fun deleteOldDiaries(): Result<Unit, DomainError>
    suspend fun getDiary(date: LocalDate): Result<Diary?, DomainError>
    fun getDiaryStream(date: LocalDate): Flow<Diary?>
    fun getDiariesByDateRangeStream(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>>
    suspend fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): Result<List<Diary>, DomainError>

    suspend fun getPagedDiaries(
        query: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        limit: Int,
        offset: Int
    ): Result<List<Diary>, DomainError>

    val diaryChangeEvent: Flow<Unit>
}