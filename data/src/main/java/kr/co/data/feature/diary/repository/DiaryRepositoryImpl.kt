package kr.co.data.feature.diary.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.data.feature.diary.mapper.DiaryDataMapper.toDiary
import kr.co.data.feature.diary.mapper.DiaryDataMapper.toDiaryEntity
import kr.co.data.feature.diary.source.remote.DiaryRemoteDataSource
import kr.co.data.feature.emotion.EmotionAnalysisAiDataSource
import kr.co.data.local.dao.DiaryDao
import kr.co.domain.feature.diary.exception.DiaryNotFoundException
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject


class DiaryRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDao,
    private val emotionAnalysisAiDataSource: EmotionAnalysisAiDataSource,
    private val diaryRemoteDataSource: DiaryRemoteDataSource,
) : DiaryRepository {

    companion object {
        private val TAG: String = DiaryRepositoryImpl::class.java.simpleName
    }

    override suspend fun hasDiaries(): Boolean {
        return diaryDao.countDiaries() > 0
    }

    override suspend fun scheduleDiaryDownload(): Result<Unit> {
        return runCatching {
            diaryRemoteDataSource.scheduleDiaryDownload()
        }.onFailure {
            Log.e(TAG, "scheduleDiaryDownload: ${it.message}")
        }
    }

    override suspend fun scheduleDiarySync(): Result<Unit> {
        return runCatching {
            diaryRemoteDataSource.scheduleDiarySync()
        }.onFailure {
            Log.e(TAG, "scheduleDiarySync: ${it.message}")
        }
    }

    override suspend fun getDiary(date: LocalDate): Result<Diary> {
        return runCatching {
            diaryDao.getDiaryByDate(date)?.toDiary()
                ?: throw DiaryNotFoundException("Diary not found with date: $date")
        }.onFailure {
            Log.e(TAG, "getDiaryByDate: ${it.message}")
        }
    }

    override fun getDiariesFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>> {
        return diaryDao.getDiariesFlowByRange(startDate, endDate).map {
            it.map { diaryEntity -> diaryEntity.toDiary() }
        }
    }

    override suspend fun upsertDiary(diary: Diary): Result<Diary> {
        return runCatching {
            val emotion = emotionAnalysisAiDataSource.emotionAnalysis(diary)
            val timestamp = System.currentTimeMillis()
            val diaryEntity = diary.toDiaryEntity()
                .copy(
                    emotion = emotion,
                    timestamp = timestamp,
                    isSynced = false,
                    isDeleted = false
                )
            val diaryId = diaryDao.upsertDiary(diaryEntity)

            diaryRemoteDataSource.scheduleDiarySync()

            diaryEntity.toDiary().copy(id = diaryId, emotion = emotion)
        }.onFailure {
            Log.e(TAG, "updateDiary: ${it.message}")
        }
    }

    override suspend fun deleteDiary(diary: Diary): Result<Unit> {
        return runCatching {
            val diaryEntity = diary.toDiaryEntity()

            diaryDao.updateDeleteStatus(diaryEntity.id, true)

            diaryRemoteDataSource.scheduleDiarySync()
        }.onFailure {
            Log.e(TAG, "deleteDiary: ${it.message}")
        }
    }
}