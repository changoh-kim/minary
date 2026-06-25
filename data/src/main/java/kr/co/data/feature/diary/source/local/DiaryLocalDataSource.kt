package kr.co.data.feature.diary.source.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiary
import kr.co.core.database.model.DiaryWithRelations
import kr.co.core.database.entity.DiaryEntity
import kr.co.core.database.entity.DiarySyncMetadataEntity
import kr.co.core.database.provider.UserDatabaseProvider
import kr.co.domain.feature.diary.model.Diary
import kr.co.core.common.state.DiarySyncStatus
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryLocalDataSource @Inject constructor(
    private val userDatabaseProvider: UserDatabaseProvider,
) {
    private val diaryDao get() = userDatabaseProvider.getDatabase().diaryDao()
    private val syncMetadataDao get() = userDatabaseProvider.getDatabase().diarySyncMetadataDao()

    fun getSyncMetadataStream(yearMonth: YearMonth) =
        syncMetadataDao.getMetadataFlow(yearMonth)

    suspend fun updateSyncMetadata(metadata: DiarySyncMetadataEntity) =
        syncMetadataDao.insertMetadata(metadata)

    suspend fun getDiary(diaryId: String): DiaryEntity? = diaryDao.getDiary(diaryId)

    suspend fun insert(entry: DiaryWithRelations) {
        diaryDao.insertDiaryWithRelations(entry)
    }

    suspend fun update(entry: DiaryWithRelations) {
        diaryDao.updateDiaryWithRelations(entry)
    }

    suspend fun upsert(entry: DiaryWithRelations) {
        diaryDao.upsertDiaryWithRelations(entry)
    }

    suspend fun delete(diaryId: String) {
        diaryDao.deleteDiary(diaryId)
    }

    suspend fun deleteOldDiaries(cutoff: Long) {
        // PENDING_* 상태 항목은 절대 삭제하지 않음
        diaryDao.deleteOldDiaries(cutoff = cutoff)
    }

    suspend fun getSyncStatus(diaryId: String): DiarySyncStatus? {
        return diaryDao.getSyncStatus(diaryId)
    }

    suspend fun getDiaryWithRelations(date: LocalDate): DiaryWithRelations? {
        return diaryDao.getDiaryWithRelations(date)
    }

    suspend fun getDiaryWithRelations(diaryId: String): DiaryWithRelations? {
        return diaryDao.getDiaryWithRelations(diaryId)
    }

    fun getDiaryStream(date: LocalDate): Flow<Diary?> {
        return diaryDao.getDiaryWithRelationsFlow(date).map { it?.toDiary() }
    }

    fun getDiariesByDateRangeStream(startDate: LocalDate, endDate: LocalDate): Flow<List<Diary>> {
        return diaryDao.getDiariesByDateRangeWithRelationsFlow(startDate, endDate).map {
            it.map { diaryEntity -> diaryEntity.toDiary() }
        }
    }

    suspend fun getDiariesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Diary> {
        return diaryDao.getDiariesByDateRangeWithRelations(startDate, endDate).map {
            it.toDiary()
        }
    }

    suspend fun getPagedDiaries(
        query: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Int,
        offset: Int
    ): List<Diary> {
        val result = if (query.isNullOrBlank() && startDate == null && endDate == null) {
            diaryDao.getAllDiariesPaged(limit, offset)
        } else {
            diaryDao.searchDiariesPaged(query ?: "", startDate, endDate, limit, offset)
        }
        return result.map { it.toDiary() }
    }

    suspend fun getPendingItemCount(): Int {
        return diaryDao.getPendingItemCount()
    }

    suspend fun getPendingDiariesWithRelations(limit: Int): List<DiaryWithRelations> {
        return diaryDao.getPendingDiariesWithRelations(limit = limit)
    }

    suspend fun markAsSynced(diaryId: String) {
        diaryDao.markAsSynced(diaryId)
    }

    suspend fun getLastModifiedAt(): Long {
        return diaryDao.getLastModifiedAt()
    }
}