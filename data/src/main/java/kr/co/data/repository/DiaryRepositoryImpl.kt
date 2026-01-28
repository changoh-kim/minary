package kr.co.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.data.local.dao.DiaryDAO
import kr.co.data.mapper.DiaryEntityMapper.toDiaryData
import kr.co.data.mapper.DiaryEntityMapper.toDiaryEntity
import kr.co.data.remote.DiaryRemoteDataSource
import kr.co.data.source.EmotionAnalysisAiDataSource
import kr.co.domain.exception.DiaryNotFoundException
import kr.co.domain.model.diary.DiaryData
import kr.co.domain.model.diary.UpsertResultDiaryData
import kr.co.domain.model.emotion.Emotion
import kr.co.domain.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject


class DiaryRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDAO,
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

    override suspend fun getDiary(date: LocalDate): Result<DiaryData> {
        return runCatching {
            diaryDao.getDiaryByDate(date)?.toDiaryData()
                ?: throw DiaryNotFoundException("DiaryData not found with date: $date")
        }.onFailure {
            Log.e(TAG, "getDiaryByDate: ${it.message}")
        }
    }

    override fun getDiariesFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<DiaryData>> {
        return diaryDao.getDiariesFlowByRange(startDate, endDate).map {
            it.map { diaryEntity -> diaryEntity.toDiaryData() }
        }
    }

    override suspend fun upsertDiary(diaryData: DiaryData): Result<UpsertResultDiaryData> {
        return runCatching {
            val emotionName = emotionAnalysisAiDataSource.emotionAnalysis(diaryData)
            val timestamp = System.currentTimeMillis()
            val diaryEntity = diaryData.toDiaryEntity()
                .copy(
                    emotionName = emotionName,
                    timestamp = timestamp,
                    isSynced = false,
                    isDeleted = false
                )

            val diaryId = diaryDao.upsertDiary(diaryEntity)
            val emotion = Emotion.fromString(emotionName)

            diaryRemoteDataSource.scheduleDiarySync()

            UpsertResultDiaryData(diaryId, emotion)
        }.onFailure {
            Log.e(TAG, "updateDiary: ${it.message}")
        }
    }

    override suspend fun deleteDiary(diaryData: DiaryData): Result<Unit> {
        return runCatching {
            val diaryEntity = diaryData.toDiaryEntity()

            diaryDao.updateDeleteStatus(diaryEntity.id, true)

            diaryRemoteDataSource.scheduleDiarySync()
        }.onFailure {
            Log.e(TAG, "deleteDiary: ${it.message}")
        }
    }
}