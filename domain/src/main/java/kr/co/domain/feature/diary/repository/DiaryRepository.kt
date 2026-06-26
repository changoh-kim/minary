package kr.co.domain.feature.diary.repository

import kr.co.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.diary.model.Diary
import kr.co.core.common.state.SyncStatus
import java.time.LocalDate
import java.time.YearMonth


interface DiaryRepository {
    fun getSyncStatusStream(yearMonth: YearMonth): Flow<SyncStatus>
    suspend fun requestMonthSync(userId: String, yearMonth: YearMonth): AppResult<Unit>

    suspend fun createDiary(diary: Diary): AppResult<Unit>
    suspend fun updateDiary(diary: Diary): AppResult<Diary>
    suspend fun deleteDiary(diary: Diary): AppResult<Unit>
    suspend fun deleteOldDiaries(): AppResult<Unit>
    suspend fun getDiary(date: LocalDate): AppResult<Diary?>
    fun getDiaryStream(date: LocalDate): Flow<Diary?>
    fun getDiariesByDateRangeStream(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>>
    suspend fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): AppResult<List<Diary>>

    suspend fun getPagedDiaries(
        query: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        limit: Int,
        offset: Int
    ): AppResult<List<Diary>>

    val diaryChangeEvent: Flow<Unit>
}