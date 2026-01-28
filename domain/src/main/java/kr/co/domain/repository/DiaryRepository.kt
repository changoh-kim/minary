package kr.co.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.domain.model.diary.DiaryData
import kr.co.domain.model.diary.UpsertResultDiaryData
import java.time.LocalDate


interface DiaryRepository {
    suspend fun hasDiaries(): Boolean
    suspend fun scheduleDiaryDownload(): Result<Unit>
    suspend fun scheduleDiarySync(): Result<Unit>
    suspend fun getDiary(date: LocalDate): Result<DiaryData>
    fun getDiariesFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<DiaryData>>
    suspend fun upsertDiary(diaryData: DiaryData): Result<UpsertResultDiaryData>
    suspend fun deleteDiary(diaryData: DiaryData): Result<Unit>
}