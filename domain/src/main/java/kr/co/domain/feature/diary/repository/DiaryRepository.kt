package kr.co.domain.feature.diary.repository

import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import kr.co.domain.error.DomainError
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.model.SyncStatus
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
    val diaryChangeEvent: Flow<Unit>
}