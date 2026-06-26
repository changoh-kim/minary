package kr.co.data.feature.diary.repository

import kr.co.core.common.logging.AppLogger
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kr.co.core.common.error.DomainError
import kr.co.core.common.result.AppResult
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.common.state.SyncStatus
import kr.co.data.extension.toDomainError
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiary
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiaryWithRelations
import kr.co.data.feature.diary.source.local.DiaryLocalDataSource
import kr.co.data.feature.emotion.source.remote.EmotionRemoteDataSource
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.diary.sync.DiarySyncManager
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import kr.co.domain.service.time.ServerTimeProvider
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepositoryImpl @Inject constructor(
    private val logger: AppLogger,
    private val diarySyncManager: DiarySyncManager,
    private val diarySyncScheduler: DiarySyncScheduler,
    private val localDataSource: DiaryLocalDataSource,
    private val emotionRemoteDataSource: EmotionRemoteDataSource,
    private val serverTime: ServerTimeProvider,
) : DiaryRepository {
    private val _diaryChangeEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val diaryChangeEvent: Flow<Unit> = _diaryChangeEvent.asSharedFlow()

    companion object {
        private val ONE_YEAR = TimeUnit.DAYS.toMillis(365)
    }

    override fun getSyncStatusStream(yearMonth: YearMonth): Flow<SyncStatus> =
        localDataSource.getSyncMetadataStream(yearMonth).map { it?.status ?: SyncStatus.IDLE }

    override suspend fun requestMonthSync(userId: String, yearMonth: YearMonth): AppResult<Unit> =
        runSuspendCatching {
            diarySyncManager.performMonthSync(userId, yearMonth)
        }.map { Unit }
        .mapError { it.toDomainError() }

    override suspend fun createDiary(diary: Diary): AppResult<Unit> =
        runSuspendCatching {
            val emotions = emotionRemoteDataSource.analysis(diary)
            val diaryWithEmotions = diary.copy(emotions = emotions)

            localDataSource.insert(diaryWithEmotions.toDiaryWithRelations())
            diarySyncScheduler.scheduleImmediateSync()
            _diaryChangeEvent.tryEmit(Unit)
        }.map { Unit }
        .mapError { it.toDomainError() }

    override suspend fun updateDiary(diary: Diary): AppResult<Diary> {
        // S4: PENDING_CREATE 상태 보호 및 데이터 존재 여부 확인
        val syncStatusResult = runSuspendCatching {
            localDataSource.getSyncStatus(diary.id)
        }.mapError { it.toDomainError() }

        return syncStatusResult.fold(
            success = { currentSyncStatus ->
                if (currentSyncStatus == null) {
                    return@fold Err(DomainError.Diary.NotFound)
                }

                runSuspendCatching {
                    val emotions = emotionRemoteDataSource.analysis(diary)

                    val syncStatus = if (currentSyncStatus == DiarySyncStatus.PENDING_CREATE) {
                        DiarySyncStatus.PENDING_CREATE
                    } else {
                        DiarySyncStatus.PENDING_UPDATE
                    }

                    val updatedDiary = diary.copy(
                        emotions = emotions,
                        syncStatus = syncStatus
                    )

                    localDataSource.update(updatedDiary.toDiaryWithRelations())
                    diarySyncScheduler.scheduleImmediateSync()
                    _diaryChangeEvent.tryEmit(Unit)

                    updatedDiary
                }
                .onErr { logger.e(it, "Failed to update diary") }
                .mapError { it.toDomainError() }
            },
            failure = { Err(it) }
        )
    }

    override suspend fun deleteDiary(diary: Diary): AppResult<Unit> =
        runSuspendCatching {
            val currentSyncStatus = localDataSource.getSyncStatus(diary.id)
            if (currentSyncStatus == DiarySyncStatus.PENDING_CREATE) {
                localDataSource.delete(diary.id)
            } else {
                localDataSource.update(
                    diary.copy(syncStatus = DiarySyncStatus.PENDING_DELETE).toDiaryWithRelations()
                )
                diarySyncScheduler.scheduleImmediateSync()
            }
            _diaryChangeEvent.tryEmit(Unit)
            Unit
        }.mapError { it.toDomainError() }

    override suspend fun deleteOldDiaries(): AppResult<Unit> =
        runSuspendCatching {
            // 1년 전까지의 데이터 삭제
            val currentTime = serverTime.now()
            val cutoff = currentTime - ONE_YEAR
            localDataSource.deleteOldDiaries(cutoff)
        }.mapError { it.toDomainError() }

    override suspend fun getDiary(date: LocalDate): AppResult<Diary?> =
        runSuspendCatching {
            localDataSource.getDiaryWithRelations(date)
        }
        .onErr { logger.e(it, "Failed to get diary") }
        .map { it?.toDiary() }
        .mapError { it.toDomainError() }

    override fun getDiaryStream(date: LocalDate): Flow<Diary?> =
        localDataSource.getDiaryStream(date)

    override fun getDiariesByDateRangeStream(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>> =
        localDataSource.getDiariesByDateRangeStream(startDate, endDate)

    override suspend fun getDiariesByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): AppResult<List<Diary>> = runSuspendCatching {
        localDataSource.getDiariesByDateRange(startDate, endDate)
    }.mapError { it.toDomainError() }

    override suspend fun getPagedDiaries(
        query: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Int,
        offset: Int
    ): AppResult<List<Diary>> = runSuspendCatching {
        localDataSource.getPagedDiaries(query, startDate, endDate, limit, offset)
    }.mapError { it.toDomainError() }
}
