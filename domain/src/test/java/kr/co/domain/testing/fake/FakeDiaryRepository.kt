package kr.co.domain.testing.fake

import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kr.co.core.common.result.AppResult
import kr.co.core.common.state.SyncStatus
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import java.time.LocalDate
import java.time.YearMonth

data class PagedDiariesRequest(
    val query: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val limit: Int,
    val offset: Int,
)

class FakeDiaryRepository(
    initialDiaries: List<Diary> = emptyList(),
) : DiaryRepository {
    private val diariesByDate = initialDiaries.associateBy { it.date }.toMutableMap()
    private val diaryStreams = mutableMapOf<LocalDate, MutableStateFlow<Diary?>>()
    private val syncStatusStreams = mutableMapOf<YearMonth, MutableStateFlow<SyncStatus>>()
    private val dateRangeStream = MutableStateFlow(initialDiaries)
    private val diaryChangeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val requestedMonthSyncs = mutableListOf<Pair<String, YearMonth>>()
    val createdDiaries = mutableListOf<Diary>()
    val updatedDiaries = mutableListOf<Diary>()
    val deletedDiaries = mutableListOf<Diary>()
    val requestedDateRanges = mutableListOf<Pair<LocalDate, LocalDate>>()
    val pagedDiaryRequests = mutableListOf<PagedDiariesRequest>()

    var requestMonthSyncResult: AppResult<Unit> = Ok(Unit)
    var createDiaryResult: AppResult<Unit> = Ok(Unit)
    var updateDiaryResult: AppResult<Diary>? = null
    var deleteDiaryResult: AppResult<Unit> = Ok(Unit)
    var deleteOldDiariesResult: AppResult<Unit> = Ok(Unit)
    var getDiaryResult: AppResult<Diary?>? = null
    var getDiariesByDateRangeResult: AppResult<List<Diary>> = Ok(initialDiaries)
    var getPagedDiariesResult: AppResult<List<Diary>> = Ok(initialDiaries)

    override fun getSyncStatusStream(yearMonth: YearMonth): Flow<SyncStatus> =
        syncStatusStreams.getOrPut(yearMonth) { MutableStateFlow(SyncStatus.IDLE) }

    fun setSyncStatus(yearMonth: YearMonth, status: SyncStatus) {
        syncStatusStreams.getOrPut(yearMonth) { MutableStateFlow(SyncStatus.IDLE) }.value = status
    }

    override suspend fun requestMonthSync(userId: String, yearMonth: YearMonth): AppResult<Unit> {
        requestedMonthSyncs += userId to yearMonth
        return requestMonthSyncResult
    }

    override suspend fun createDiary(diary: Diary): AppResult<Unit> {
        createdDiaries += diary
        diariesByDate[diary.date] = diary
        diaryStreams[diary.date]?.value = diary
        return createDiaryResult
    }

    override suspend fun updateDiary(diary: Diary): AppResult<Diary> {
        updatedDiaries += diary
        diariesByDate[diary.date] = diary
        diaryStreams[diary.date]?.value = diary
        return updateDiaryResult ?: Ok(diary)
    }

    override suspend fun deleteDiary(diary: Diary): AppResult<Unit> {
        deletedDiaries += diary
        diariesByDate.remove(diary.date)
        diaryStreams[diary.date]?.value = null
        return deleteDiaryResult
    }

    override suspend fun deleteOldDiaries(): AppResult<Unit> = deleteOldDiariesResult

    override suspend fun getDiary(date: LocalDate): AppResult<Diary?> =
        getDiaryResult ?: Ok(diariesByDate[date])

    override fun getDiaryStream(date: LocalDate): Flow<Diary?> =
        diaryStreams.getOrPut(date) { MutableStateFlow(diariesByDate[date]) }

    fun emitDiary(date: LocalDate, diary: Diary?) {
        if (diary == null) {
            diariesByDate.remove(date)
        } else {
            diariesByDate[date] = diary
        }
        diaryStreams.getOrPut(date) { MutableStateFlow(null) }.value = diary
    }

    override fun getDiariesByDateRangeStream(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<Diary>> {
        requestedDateRanges += startDate to endDate
        return dateRangeStream
    }

    fun emitDateRangeDiaries(diaries: List<Diary>) {
        dateRangeStream.value = diaries
    }

    override suspend fun getDiariesByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): AppResult<List<Diary>> {
        requestedDateRanges += startDate to endDate
        return getDiariesByDateRangeResult
    }

    override suspend fun getPagedDiaries(
        query: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Int,
        offset: Int,
    ): AppResult<List<Diary>> {
        pagedDiaryRequests += PagedDiariesRequest(
            query = query,
            startDate = startDate,
            endDate = endDate,
            limit = limit,
            offset = offset,
        )
        return getPagedDiariesResult
    }

    override val diaryChangeEvent: Flow<Unit> = diaryChangeEvents

    fun emitDiaryChange() {
        diaryChangeEvents.tryEmit(Unit)
    }
}
