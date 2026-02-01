package kr.co.domain.feature.diary.repository

import kotlinx.coroutines.flow.Flow
import kr.co.domain.feature.diary.model.Diary
import java.time.LocalDate


interface DiaryRepository {
    suspend fun hasDiaries(): Boolean
    suspend fun scheduleDiaryDownload(): Result<Unit>
    suspend fun scheduleDiarySync(): Result<Unit>
    suspend fun getDiary(date: LocalDate): Result<Diary>
    fun getDiariesFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>>
    suspend fun upsertDiary(diary: Diary): Result<Diary>
    suspend fun deleteDiary(diary: Diary): Result<Unit>
}