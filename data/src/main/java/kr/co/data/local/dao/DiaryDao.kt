package kr.co.data.local.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kr.co.data.feature.diary.source.local.model.DiaryWithRelations
import kr.co.data.feature.emotion.source.local.model.EmotionStats
import kr.co.data.local.entity.DiaryEmotionEntity
import kr.co.data.local.entity.DiaryEntity
import kr.co.data.local.entity.DiaryImageUrlEntity
import kr.co.domain.feature.diary.model.DiarySyncStatus
import java.time.LocalDate

@Dao
abstract class DiaryDao {

    companion object {
        private const val SYNCED = "SYNCED"
        private const val PENDING_DELETE = "PENDING_DELETE"
    }



    // ───────────────────────────────────────────────────────────────────────────────────
    // DiaryEntity 삽입, 수정
    //
    // Protected: 외부에서 직접 호출 금지. @Transaction 함수를 통해서만 사용
    // ───────────────────────────────────────────────────────────────────────────────────
    /**
     * @suppress 직접 호출 금지. [insertDiaryWithRelations]을 사용하세요.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertDiary(diary: DiaryEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertDiary(diaries: List<DiaryEntity>)

    @Update
    protected abstract suspend fun updateDiary(diary: DiaryEntity): Int

    @Update
    protected abstract suspend fun updateDiary(diaries: List<DiaryEntity>): Int

    @Upsert
    protected abstract suspend fun upsertDiary(diary: DiaryEntity)

    @Upsert
    protected abstract suspend fun upsertDiary(diaries: List<DiaryEntity>)



    // ───────────────────────────────────────────────────────────────────────────────────
    // DiaryEmotionEntity 단독 조작
    //
    // Protected: 외부에서 직접 호출 금지. @Transaction 함수를 통해서만 사용
    // ───────────────────────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertEmotions(emotions: List<DiaryEmotionEntity>)

    @Query(
        """
        DELETE FROM ${DiaryEmotionEntity.TABLE_NAME}
        WHERE ${DiaryEmotionEntity.COLUMN_DIARY_ID} = :diaryId
        """
    )
    protected abstract suspend fun deleteEmotions(diaryId: String)

    @Query(
        """
        DELETE FROM ${DiaryEmotionEntity.TABLE_NAME}
        WHERE ${DiaryEmotionEntity.COLUMN_DIARY_ID} IN (:diaryIds)
        """
    )
    protected abstract suspend fun deleteEmotions(diaryIds: List<String>)



    // ───────────────────────────────────────────────────────────────────────────────────
    // DiaryImageUrlEntity 단독 조작
    //
    // Protected: 외부에서 직접 호출 금지. @Transaction 함수를 통해서만 사용
    // ───────────────────────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertImageUrls(imageUrls: List<DiaryImageUrlEntity>)

    @Query(
        """
        DELETE FROM ${DiaryImageUrlEntity.TABLE_NAME}
        WHERE ${DiaryImageUrlEntity.COLUMN_DIARY_ID} = :diaryId
        """
    )
    protected abstract suspend fun deleteImageUrls(diaryId: String)

    @Query(
        """
        DELETE FROM ${DiaryImageUrlEntity.TABLE_NAME}
        WHERE ${DiaryImageUrlEntity.COLUMN_DIARY_ID} IN (:diaryIds)
        """
    )
    protected abstract suspend fun deleteImageUrls(diaryIds: List<String>)



    // ───────────────────────────────────────────────────────────────────────────────────
    // Diary, Emotions, ImageUrls 삽입, 수정
    //
    // Internal: 같은 모듈 일 때 Dao 외부에서 직접 호출 가능
    // ───────────────────────────────────────────────────────────────────────────────────

    /**
     * 일기, 감정, 이미지 URL을 함께 삽입합니다.
     *
     * 1. [diary]를 diary 테이블에 삽입합니다.
     * 2. [emotions]를 diaryEmotions 테이블에 삽입합니다.
     * 3. [imageUrls]를 diaryImageUrls 테이블에 삽입합니다.
     * 4. 하나라도 실패하면 전체 롤백됩니다.
     *
     * @throws SQLiteConstraintException id 또는 date 충돌 시
     */
    @Transaction
    internal open suspend fun insertDiaryWithRelations(
        diary: DiaryEntity,
        emotions: List<DiaryEmotionEntity>,
        imageUrls: List<DiaryImageUrlEntity>
    ) {
        insertDiary(diary)
        insertEmotions(emotions)
        insertImageUrls(imageUrls)
    }

    /**
     * 일기, 감정, 이미지 URL을 함께 삽입합니다.
     *
     * [entry]의 데이터를 관계된 테이블에 삽입합니다.
     * 하나라도 실패하면 전체 롤백됩니다.
     *
     * @throws SQLiteConstraintException id 또는 date 충돌 시
     */
    @Transaction
    internal open suspend fun insertDiaryWithRelations(
        entry: DiaryWithRelations,
    ) {
        insertDiaryWithRelations(
            entry.diary,
            entry.emotions,
            entry.imageUrls,
        )
    }

    /**
     * 일기, 감정, 이미지 URL을 함께 수정합니다.
     *
     * 1. [diary]를 diary 테이블에 수정합니다.
     * 2. [emotions]를 diaryEmotions 테이블에 수정합니다.
     * 3. [imageUrls]를 diaryImageUrls 테이블에 수정합니다.
     * 4. 하나라도 실패하면 전체 롤백됩니다.
     *
     * @throws SQLiteConstraintException date 충돌 시
     */
    @Transaction
    internal open suspend fun updateDiaryWithRelations(
        diary: DiaryEntity,
        emotions: List<DiaryEmotionEntity>,
        imageUrls: List<DiaryImageUrlEntity>
    ) {
        updateDiary(diary)
        // 기존 감정/이미지 전체 삭제 후 재삽입 (변경 반영)
        deleteEmotions(diary.id)
        insertEmotions(emotions)

        deleteImageUrls(diary.id)
        insertImageUrls(imageUrls)
    }

    /**
     * 일기, 감정, 이미지 URL을 함께 수정합니다.
     *
     * [entry]의 데이터를 관계된 테이블에 수정합니다.
     * 하나라도 실패하면 전체 롤백됩니다.
     *
     * @throws SQLiteConstraintException date 충돌 시
     */
    @Transaction
    internal open suspend fun updateDiaryWithRelations(
        entry: DiaryWithRelations,
    ) {
        updateDiaryWithRelations(
            entry.diary,
            entry.emotions,
            entry.imageUrls,
        )
    }

    /**
     * 일기, 감정, 이미지 URL을 함께 작성하거나 수정합니다.
     *
     * 로컬 데이터가 존재 여부에 따라
     * [entry]의 데이터를 관계된 테이블에 작성 또는 수정합니다.
     * 하나라도 실패하면 전체 롤백됩니다.
     *
     * @throws SQLiteConstraintException date 충돌 시
     */
    @Transaction
    internal open suspend fun upsertDiaryWithRelations(
        entry: DiaryWithRelations,
    ) {
        upsertDiaryWithRelations(
            entry.diary,
            entry.emotions,
            entry.imageUrls,
        )
    }

    /**
     * 일기, 감정, 이미지 URL을 함께 작성하거나 수정합니다.
     *
     * 로컬 데이터가 존재 여부에 따라
     * [diary]와 관계된 테이블에 작성 또는 수정합니다.
     * 하나라도 실패하면 전체 롤백됩니다.
     *
     * @throws SQLiteConstraintException date 충돌 시
     */
    @Transaction
    internal open suspend fun upsertDiaryWithRelations(
        diary: DiaryEntity,
        emotions: List<DiaryEmotionEntity>,
        imageUrls: List<DiaryImageUrlEntity>,
    ) {
        val localDiary = getDiary(diary.id)
        if (localDiary == null) {
            // 로컬에 없는 새 일기 → 삽입
            insertDiaryWithRelations(
                diary = diary,
                emotions = emotions,
                imageUrls = imageUrls
            )
        } else {
            updateDiaryWithRelations(
                diary = diary,
                emotions = emotions,
                imageUrls = imageUrls
            )
        }
    }

    /**
     * 일기 목록을 로컬에 일괄 동기화합니다.
     * 각 항목별로 [upsertDiaryWithRelations]를 적용하며, 전체가 하나의 트랜잭션으로 처리됩니다.
     */
    @Transaction
    internal open suspend fun upsertDiariesWithRelations(
        items: List<DiaryWithRelations>,
    ) {
        items.forEach { upsertDiaryWithRelations(it) }
    }

    /**
     * 일기 목록을 로컬에 일괄 동기화합니다.
     * 각 항목별로 [upsertDiaryWithRelations]를 적용하며, 전체가 하나의 트랜잭션으로 처리됩니다.
     */
    @Transaction
    internal open suspend fun upsertDiariesWithRelationsFromRaw(
        items: List<Triple<DiaryEntity, List<DiaryEmotionEntity>, List<DiaryImageUrlEntity>>>,
    ) {
        items.forEach { (diary, emotions, imageUrls) ->
            upsertDiaryWithRelations(diary, emotions, imageUrls)
        }
    }



    // ───────────────────────────────────────────────────────────────────────────────────
    // DiaryEntity, Emotions, ImageUrls 삭제
    //
    // ForeignKey.CASCADE 설정으로
    // Diary 삭제시 Emotions, ImageUrls 자동 삭제
    //
    // Internal: 같은 모듈 일 때 Dao 외부에서 직접 호출 가능
    // ───────────────────────────────────────────────────────────────────────────────────

    /**
     * 일기, 감정, 이미지 URL을 함께 삭제합니다.
     *
     * [DiaryEmotionEntity], [DiaryImageUrlEntity] ForeignKey CASCADE 설정으로
     * diaryEmotions, diaryImageUrls 테이블은 diary 테이블 삭제 시 자동 삭제됩니다.
     */
    @Query(
        """
        DELETE FROM ${DiaryEntity.TABLE_NAME} 
        WHERE ${DiaryEntity.COLUMN_ID} = :id
        """
    )
    internal abstract suspend fun deleteDiary(id: String): Int

    /**
     * 일기, 감정, 이미지 URL을 함께 삭제합니다.
     *
     * [DiaryEmotionEntity], [DiaryImageUrlEntity] ForeignKey CASCADE 설정으로
     * diaryEmotions, diaryImageUrls 테이블은 diary 테이블 삭제 시 자동 삭제됩니다.
     */
    @Query(
        """
        DELETE FROM ${DiaryEntity.TABLE_NAME} 
        WHERE ${DiaryEntity.COLUMN_ID} IN (:ids)
        """
    )
    internal abstract suspend fun deleteDiariesByIds(ids: List<String>): Int

    /**
     * 일기, 감정, 이미지 URL을 함께 삭제합니다.
     *
     * [DiaryEmotionEntity], [DiaryImageUrlEntity] ForeignKey CASCADE 설정으로
     * diaryEmotions, diaryImageUrls 테이블은 diary 테이블 삭제 시 자동 삭제됩니다.
     */
    @Delete
    internal abstract suspend fun deleteDiary(diary: DiaryEntity): Int

    /**
     * 일기, 감정, 이미지 URL을 함께 삭제합니다.
     *
     * [DiaryEmotionEntity], [DiaryImageUrlEntity] ForeignKey CASCADE 설정으로
     * diaryEmotions, diaryImageUrls 테이블은 diary 테이블 삭제 시 자동 삭제됩니다.
     */
    @Delete
    internal abstract suspend fun deleteDiaries(diaries: List<DiaryEntity>): Int

    /**
     * 기준 시간([cutoff])보다 오래된 일기 중, 서버와 동기화가 완료된 데이터를 삭제합니다.
     *
     * 1. 로컬 저장 공간 관리 정책(예: 1년치 데이터 유지)을 위해 사용됩니다.
     * 2. 서버에 업로드되지 않은 '동기화 대기' 상태의 항목은 절대 삭제하지 않습니다.
     * 3. [DiaryEntity] 삭제 시, ForeignKey CASCADE 설정에 의해 연관된 감정([DiaryEmotionEntity]) 및
     *    이미지 URL([DiaryImageUrlEntity]) 데이터도 데이터베이스에서 자동으로 함께 삭제됩니다.
     *
     * @param cutoff 이 시간보다 이전에 수정된(timestamp) 데이터를 삭제 대상으로 합니다.
     * @return 삭제된 일기의 개수를 반환합니다.
     */
    @Transaction
    @Query(
        """
        DELETE FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_LAST_MODIFIED_AT} < :cutoff
        AND ${DiaryEntity.COLUMN_SYNC_STATUS} = '$SYNCED'
        """
    )
    internal abstract suspend fun deleteOldDiaries(cutoff: Long): Int



    // ───────────────────────────────────────────────────────────────────────────────────
    // 조회 관련 함수
    //
    // Internal: 같은 모듈 일 때 Dao 외부에서 직접 호출 가능
    // ───────────────────────────────────────────────────────────────────────────────────

    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_ID} = :id
        """
    )
    internal abstract suspend fun getDiary(id: String): DiaryEntity?

    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_DATE} = :date
        AND ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        """
    )
    internal abstract suspend fun getDiary(date: LocalDate): DiaryEntity?

    /**
     * 특정 일기의 모든 감정을 조회합니다.
     */
    @Query(
        """
        SELECT * FROM ${DiaryEmotionEntity.TABLE_NAME}
        WHERE ${DiaryEmotionEntity.COLUMN_DIARY_ID} = :diaryId
        """
    )
    internal abstract suspend fun getEmotions(diaryId: String): List<DiaryEmotionEntity>

    /**
     * 특정 일기의 모든 이미지 URL을 조회합니다.
     */
    @Query(
        """
        SELECT * FROM ${DiaryImageUrlEntity.TABLE_NAME}
        WHERE ${DiaryImageUrlEntity.COLUMN_DIARY_ID} = :diaryId
        """
    )
    internal abstract suspend fun getImageUrls(diaryId: String): List<DiaryImageUrlEntity>

    /**
     * [id]로 일기, 감정, 이미지 URL을 함께 조회합니다.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_ID} = :id
        AND ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        """
    )
    internal abstract suspend fun getDiaryWithRelations(id: String): DiaryWithRelations?

    /**
     * [date]로 일기, 감정, 이미지 URL을 함께 조회합니다.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_DATE} = :date
        AND ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        """
    )
    internal abstract suspend fun getDiaryWithRelations(date: LocalDate): DiaryWithRelations?

    /**
     * [id]로 일기, 감정, 이미지 URL을 함께 관찰하는 Flow를 생성합니다.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_ID} = :id
        AND ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        """
    )
    internal abstract fun getDiaryWithRelationsFlow(id: String): Flow<DiaryWithRelations?>

    /**
     * [date]로 일기, 감정, 이미지 URL을 함께 관찰하는 Flow를 생성합니다.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_DATE} = :date
        AND ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        """
    )
    internal abstract fun getDiaryWithRelationsFlow(date: LocalDate): Flow<DiaryWithRelations?>

    /**
     * 전체 일기 목록을 내림차순(날짜: 현재에서 과거순)으로 반환합니다.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        ORDER BY ${DiaryEntity.COLUMN_DATE} DESC
        """
    )
    internal abstract suspend fun getAllDiariesWithRelations(): List<DiaryWithRelations>

    /**
     * 전체 일기 목록을 내림차순으로 관찰하는 Flow를 생성합니다.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        ORDER BY ${DiaryEntity.COLUMN_DATE} DESC
        """
    )
    internal abstract fun getAllDiariesWithRelationsFlow(): Flow<List<DiaryWithRelations>>

    /**
     * 특정 기간 일기 목록을 내림차순으로 반환합니다.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_DATE} BETWEEN :startDate AND :endDate
        AND ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        ORDER BY ${DiaryEntity.COLUMN_DATE} DESC
        """
    )
    internal abstract suspend fun getDiariesByDateRangeWithRelations(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DiaryWithRelations>

    /**
     * 특정 기간 일기 목록을 내림차순으로 관찰하는 Flow를 생성합니다.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_DATE} BETWEEN :startDate AND :endDate
        AND ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        ORDER BY ${DiaryEntity.COLUMN_DATE} DESC
        """
    )
    internal abstract fun getDiariesByDateRangeWithRelationsFlow(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DiaryWithRelations>>

    /**
     * 동기화 대기 중인 일기 목록을 관련 데이터와 함께 가져옵니다.
     * 수정된 시간이 오래된 순서(ASC)로 정렬하여 동기화 순서를 보장하며,
     * 한 번에 처리할 개수를 [limit]으로 제한합니다.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_SYNC_STATUS} != '$SYNCED'
        ORDER BY ${DiaryEntity.COLUMN_LAST_MODIFIED_AT} ASC
        LIMIT :limit
        """
    )
    internal abstract suspend fun getPendingDiariesWithRelations(limit: Int = 50): List<DiaryWithRelations>



    // ─────────────────────────────────────
    // 통계 관련 함수
    //
    // Internal: 같은 모듈 일 때 Dao 외부에서 직접 호출 가능
    // ─────────────────────────────────────

    @Query(
        """
        SELECT COUNT(*) 
        FROM ${DiaryEntity.TABLE_NAME} 
        WHERE ${DiaryEntity.COLUMN_DATE} BETWEEN :startDate AND :endDate 
        AND ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        """
    )
    internal abstract suspend fun getDiaryCountByDateRange(startDate: LocalDate, endDate: LocalDate): Int

    @Query("SELECT COUNT(*) FROM ${DiaryEntity.TABLE_NAME}")
    internal abstract suspend fun getTotalDiaryCount(): Int

    @Query(
        """
        SELECT ${DiaryEntity.COLUMN_DATE} 
        FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        ORDER BY ${DiaryEntity.COLUMN_DATE} DESC
        """
    )
    internal abstract suspend fun getAllDiaryDates(): List<LocalDate>

    @Query(
        """
        SELECT IFNULL(SUM(LENGTH(${DiaryEntity.COLUMN_CONTENT})), 0)
        FROM ${DiaryEntity.TABLE_NAME}
        """
    )
    internal abstract suspend fun getTotalWordCount(): Int

    @Query(
        """
        SELECT IFNULL(SUM(LENGTH(${DiaryEntity.COLUMN_CONTENT})), 0)
        FROM ${DiaryEntity.TABLE_NAME}
        """
    )
    internal abstract fun getWordTotalCountFlow(): Flow<Int>

    /**
     * 가장 많이 기록된 감정과 횟수를 담은 [EmotionStats]객체를 반환합니다.
     * 빈도수가 같을 경우 가장 최근에 기록된 감정을 우선합니다.
     */
    @Query(
        """
        SELECT e.${DiaryEmotionEntity.COLUMN_EMOTION}, COUNT(*) as count 
        FROM ${DiaryEmotionEntity.TABLE_NAME} AS e
        INNER JOIN ${DiaryEntity.TABLE_NAME} AS d ON e.${DiaryEmotionEntity.COLUMN_DIARY_ID} = d.${DiaryEntity.COLUMN_ID}
        GROUP BY e.${DiaryEmotionEntity.COLUMN_EMOTION} 
        ORDER BY count DESC, MAX(d.${DiaryEntity.COLUMN_LAST_MODIFIED_AT}) DESC 
        LIMIT 1
        """
    )
    internal abstract suspend fun getMostFrequentEmotionStats(): EmotionStats?

    /**
     * 가장 적게 기록된 감정과 횟수를 담은 [EmotionStats]객체를 반환합니다.
     * 빈도수가 같을 경우 가장 최근에 기록된 감정을 우선합니다.
     */
    @Query(
        """
        SELECT e.${DiaryEmotionEntity.COLUMN_EMOTION}, COUNT(*) as count 
        FROM ${DiaryEmotionEntity.TABLE_NAME} AS e
        INNER JOIN ${DiaryEntity.TABLE_NAME} AS d ON e.${DiaryEmotionEntity.COLUMN_DIARY_ID} = d.${DiaryEntity.COLUMN_ID}
        GROUP BY e.${DiaryEmotionEntity.COLUMN_EMOTION} 
        ORDER BY count ASC, MAX(d.${DiaryEntity.COLUMN_LAST_MODIFIED_AT}) DESC 
        LIMIT 1
        """
    )
    internal abstract suspend fun getLeastFrequentEmotionStaus(): EmotionStats?

    /**
     * 전체 감정 빈도 랭킹을 반환합니다.
     */
    @Query("""
        SELECT ${DiaryEmotionEntity.COLUMN_EMOTION}, COUNT(*) as count 
        FROM ${DiaryEmotionEntity.TABLE_NAME}
        GROUP BY ${DiaryEmotionEntity.COLUMN_EMOTION}
        ORDER BY count DESC
    """)
    internal abstract suspend fun getEmotionStatistics(): List<EmotionStats>

    /**
     * 특정 기간 동안 가장 많이 기록된 감정과 횟수를 반환합니다.
     */
    @Query("""
        SELECT e.${DiaryEmotionEntity.COLUMN_EMOTION}, COUNT(*) as count
        FROM ${DiaryEmotionEntity.TABLE_NAME} e
        INNER JOIN ${DiaryEntity.TABLE_NAME} d ON e.${DiaryEmotionEntity.COLUMN_DIARY_ID} = d.${DiaryEntity.COLUMN_ID}
        WHERE d.${DiaryEntity.COLUMN_DATE} BETWEEN :startDate AND :endDate
        GROUP BY e.${DiaryEmotionEntity.COLUMN_EMOTION}
        ORDER BY count DESC, MAX(d.${DiaryEntity.COLUMN_LAST_MODIFIED_AT}) DESC
        LIMIT 1
    """)
    internal abstract suspend fun getEmotionStatisticsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): EmotionStats?



    // ───────────────────────────────────────────────────────────────────────────────────
    // 동기화 관련 함수
    //
    // Internal: 같은 모듈 일 때 Dao 외부에서 직접 호출 가능
    // ───────────────────────────────────────────────────────────────────────────────────

    /**
     * 특정 일기의 동기화 상태를 조회합니다.
     */
    @Query(
        """
        SELECT ${DiaryEntity.COLUMN_SYNC_STATUS} FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_ID} = :id
        """
    )
    internal abstract suspend fun getSyncStatus(id: String): DiarySyncStatus?

    /**
     * 특정 일기의 동기화 상태를 조회합니다.
     */
    internal suspend fun getSyncStatus(diary: DiaryEntity): DiarySyncStatus? {
        return getSyncStatus(diary.id)
    }

    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE (${DiaryEntity.COLUMN_TITLE} LIKE '%' || :query || '%' OR ${DiaryEntity.COLUMN_CONTENT} LIKE '%' || :query || '%')
        AND (${DiaryEntity.COLUMN_DATE} BETWEEN :startDate AND :endDate OR (:startDate IS NULL OR :endDate IS NULL))
        AND ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        ORDER BY ${DiaryEntity.COLUMN_DATE} DESC
        LIMIT :limit OFFSET :offset
        """
    )
    internal abstract suspend fun searchDiariesPaged(
        query: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Int,
        offset: Int
    ): List<DiaryWithRelations>

    @Transaction
    @Query(
        """
        SELECT * FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_SYNC_STATUS} != '$PENDING_DELETE'
        ORDER BY ${DiaryEntity.COLUMN_DATE} DESC
        LIMIT :limit OFFSET :offset
        """
    )
    internal abstract suspend fun getAllDiariesPaged(
        limit: Int,
        offset: Int
    ): List<DiaryWithRelations>

    @Query(
        """
        SELECT MAX(${DiaryEntity.COLUMN_LAST_MODIFIED_AT}) 
        FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_SYNC_STATUS} = '$SYNCED'
        """
    )
    internal abstract suspend fun getLastSyncedAt(): Long?

    @Query(
        """
        SELECT COUNT(*) FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_SYNC_STATUS} != '$SYNCED'
        """
    )
    internal abstract suspend fun getPendingItemCount(): Int

    @Query(
        """
        SELECT COUNT(*) FROM ${DiaryEntity.TABLE_NAME}
        WHERE ${DiaryEntity.COLUMN_SYNC_STATUS} != '$SYNCED'
        """
    )
    internal abstract fun getPendingItemCountFlow(): Flow<Int>

    @Query(
        """
        UPDATE ${DiaryEntity.TABLE_NAME} 
        SET ${DiaryEntity.COLUMN_SYNC_STATUS} = '$SYNCED'
        WHERE ${DiaryEntity.COLUMN_ID} = :id
        """
    )
    internal abstract suspend fun markAsSynced(id: String): Int

    @Query(
        """
        UPDATE ${DiaryEntity.TABLE_NAME} 
        SET ${DiaryEntity.COLUMN_SYNC_STATUS} = '$SYNCED'
        WHERE ${DiaryEntity.COLUMN_DATE} = :date
        """
    )
    internal abstract suspend fun markAsSynced(date: LocalDate): Int

    @Query(
        """
        UPDATE ${DiaryEntity.TABLE_NAME} 
        SET ${DiaryEntity.COLUMN_SYNC_STATUS} = '$SYNCED'
        WHERE ${DiaryEntity.COLUMN_ID} IN (:ids)
        """
    )
    internal abstract suspend fun markAsSyncedByIds(ids: List<String>): Int

    @Query(
        """
        SELECT IFNULL(MAX(${DiaryEntity.COLUMN_LAST_MODIFIED_AT}), 0)
        FROM ${DiaryEntity.TABLE_NAME}
        """
    )
    internal abstract suspend fun getLastModifiedAt(): Long

    internal open suspend fun markAsSynced(diaries: List<DiaryEntity>) {
        markAsSyncedByIds(diaries.map { it.id })
    }
}