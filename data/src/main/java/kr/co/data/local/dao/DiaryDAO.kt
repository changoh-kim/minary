package kr.co.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kr.co.data.model.diary.DiaryEntity
import java.time.LocalDate


@Dao
interface DiaryDAO {

    /**
     * 로컬 DB에 저장된 일기의 총 개수를 반환합니다.
     * @return 일기 개수
     */
    @Query("SELECT COUNT(id) FROM diary")
    suspend fun countDiaries(): Int

    /**
     * [date]에 해당하는 일기 중 삭제되지 않은 일기를 조회합니다.
     *
     * @return 데이터가 있으면 DiaryEntity, 없으면 null을 반환합니다.
     */
    @Query("SELECT * FROM diary WHERE date = :date AND isDeleted = 0")
    suspend fun getDiaryByDate(date: LocalDate): DiaryEntity?

    /**
     * [startDate]와 [endDate] 사이의 삭제되지 않은 모든 일기를 Flow 형태로 조회합니다.
     *
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return diary 테이블의 데이터가 변경되면 Flow가 자동으로 새 목록을 방출합니다.
     */
    @Query("SELECT * FROM diary WHERE date BETWEEN :startDate AND :endDate AND isDeleted = 0")
    fun getDiariesFlowByRange(startDate: LocalDate, endDate: LocalDate): Flow<List<DiaryEntity>>

    /**
     * 서버와 동기화되지 않은 모든 일기를 조회합니다.
     *
     * @return 동기화되지 않은 일기 목록. 없으면 null을 반환합니다.
     */
    @Query("SELECT * FROM diary WHERE isSynced = 0")
    suspend fun getUnsyncedDiaries(): List<DiaryEntity>?

    /**
     * 주어진 [ids] 목록에 해당하는 모든 일기의 `isSynced` 상태를 한 번에 업데이트합니다.
     *
     * @param ids 업데이트할 일기 ID 목록
     * @param isSynced 적용할 동기화 상태
     * @return 업데이트된 행의 개수를 반환합니다.
     */
    @Query("UPDATE diary SET isSynced = :isSynced WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<Long>, isSynced: Boolean): Int

    /**
     * 주어진 [id]에 해당하는 일기를 삭제 상태([isDeleted])로 변경하고,
     * 동기화가 필요하도록 `isSynced` 상태를 `false`로 설정합니다.
     *
     * @param id 업데이트할 일기 ID
     * @param isDeleted 적용할 삭제 상태
     * @return 업데이트된 행의 개수를 반환합니다.
     */
    @Query("UPDATE diary SET isDeleted = :isDeleted, isSynced = 0 WHERE id = :id")
    suspend fun updateDeleteStatus(id: Long, isDeleted: Boolean): Int

    /**
     * [diaryEntity]를 데이터베이스에 삽입하거나, Primary Key가 이미 존재하면 업데이트합니다. (Upsert)
     *
     * @return 추가되거나 업데이트된 행의 ID를 반환합니다.
     */
    @Upsert
    suspend fun upsertDiary(diaryEntity: DiaryEntity): Long

    /**
     * 주어진 [diaries] 목록을 데이터베이스에 삽입하거나, Primary Key가 이미 존재하면 업데이트합니다. (Upsert)
     *
     * @param diaries Upsert할 DiaryEntity 목록
     * @return 추가되거나 업데이트된 행의 ID 목록을 반환합니다.
     */
    @Upsert
    suspend fun upsertDiaries(diaries: List<DiaryEntity>): List<Long>

    /**
     * 주어진 [diaryEntity]와 일치하는 일기를 데이터베이스에서 삭제합니다.
     *
     * @return 삭제된 행의 개수를 반환합니다.
     */
    @Delete
    suspend fun deleteDiary(diaryEntity: DiaryEntity): Int

    /**
     * 주어진 [ids] 목록과 일치하는 모든 일기를 데이터베이스에서 삭제합니다.
     *
     * @param ids 삭제할 일기 ID 목록
     * @return 삭제된 행의 개수를 반환합니다.
     */
    @Query("DELETE FROM diary WHERE id IN (:ids)")
    suspend fun deleteDiariesByIds(ids: List<Long>): Int
}