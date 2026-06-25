package kr.co.data.feature.diary.sync

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kr.co.data.extension.toDomainError
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiaryDto
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiaryWithRelations
import kr.co.data.feature.diary.model.DiaryDto
import kr.co.data.feature.diary.source.local.DiaryLocalDataSource
import kr.co.core.database.model.DiaryWithRelations
import kr.co.core.database.entity.DiarySyncMetadataEntity
import kr.co.core.datastore.sync.UserSyncDataStoreProvider
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.core.common.error.DomainError
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.common.state.SyncStatus
import kr.co.domain.feature.diary.sync.DiarySyncManager
import kr.co.domain.service.time.ServerTimeProvider
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDiarySyncManager @Inject constructor(
    private val diaryLocalDataSource: DiaryLocalDataSource,
    private val userDataStoreProvider: UserSyncDataStoreProvider,
    private val firebaseFirestoreProvider: FirebaseFirestoreProvider,
    private val serverTime: ServerTimeProvider,
): DiarySyncManager {

    companion object {
        const val CHUNK_SIZE = 50
    }

    private val dataStore get() = userDataStoreProvider.getDataStore()

    override suspend fun performChunkedSync(userId: String): Result<Boolean, DomainError> =
        coroutineBinding {
            pushChunk(userId).bind()

            val hasPendingPush = diaryLocalDataSource.getPendingItemCount() > 0
            val hasPendingPull = pullChunk(userId).bind()

            val hasMore = hasPendingPush || hasPendingPull

            hasMore
        }

    override suspend fun performImmediatePush(userId: String): Result<Unit, DomainError> {
        return pushChunk(userId)
    }

    override suspend fun performMonthSync(
        userId: String,
        yearMonth: YearMonth
    ): Result<Unit, DomainError> = runSuspendCatching {
        // 1. Metadata 상태를 LOADING으로 변경
        diaryLocalDataSource.updateSyncMetadata(
            DiarySyncMetadataEntity(yearMonth, SyncStatus.LOADING)
        )

        val startOfMonth = yearMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC).toEpochMilli()

        val remoteDocs = firebaseFirestoreProvider.getDiariesRef(userId)
            .whereGreaterThanOrEqualTo(DiaryDto.CREATED_AT, startOfMonth)
            .whereLessThanOrEqualTo(DiaryDto.CREATED_AT, endOfMonth)
            .get()
            .await()

        remoteDocs.documents.forEach { doc ->
            val remoteUpdatedAt = doc.getLong(DiaryDto.LAST_MODIFIED_AT) ?: return@forEach
            val relations = diaryLocalDataSource.getDiaryWithRelations(doc.id)
            val localEntry = relations?.diary

            // [개선 포인트] LWW 정책으로 단일화
            // 로컬 데이터가 존재하고, 로컬의 수정 시각이 서버의 수정 시각보다 크거나 같다면
            // 로컬 데이터가 최신이므로 서버 데이터를 무시(skip)합니다.
            if (localEntry != null && localEntry.lastModifiedAt >= remoteUpdatedAt) {
                return@forEach
            }

            // 그 외의 경우(로컬에 없거나, 서버 데이터가 더 최신인 경우)는 서버 데이터를 로컬에 반영합니다.
            val remoteDiaryDto = doc.toObject(DiaryDto::class.java)
            remoteDiaryDto?.let {
                diaryLocalDataSource.upsert(
                    it.toDiaryWithRelations(DiarySyncStatus.SYNCED)
                )
            }
        }

        // 2. Metadata 상태를 SYNCED로 변경
        diaryLocalDataSource.updateSyncMetadata(
            DiarySyncMetadataEntity(yearMonth, SyncStatus.SYNCED)
        )
    }.mapError {
        // 실패 시 FAILED 상태로 업데이트
        diaryLocalDataSource.updateSyncMetadata(
            DiarySyncMetadataEntity(yearMonth, SyncStatus.FAILED)
        )
        it.toDomainError()
    }

    private suspend fun pushChunk(userId: String): Result<Unit, DomainError> {
        val pendingItems = diaryLocalDataSource.getPendingDiariesWithRelations(limit = CHUNK_SIZE)
        if (pendingItems.isEmpty()) return Ok(Unit)

        return processFirestoreBatch(userId, pendingItems)
    }

    private suspend fun processFirestoreBatch(
        userId: String,
        items: List<DiaryWithRelations>
    ): Result<Unit, DomainError> = coroutineScope {
        if (items.isEmpty()) return@coroutineScope Ok(Unit)

        val batch = firebaseFirestoreProvider.batch()
        val diariesRef = firebaseFirestoreProvider.getDiariesRef(userId)

        // PENDING_UPDATE 문서의 lastModifiedAt 수집
        val serverUpdatedAts: Map<String, Long> = items
            .filter { it.diary.syncStatus == DiarySyncStatus.PENDING_UPDATE }
            .map { relations ->
                async {
                    val docId = relations.diary.id
                    val serverUpdatedAt = diariesRef.document(docId)
                        .get()
                        .await()
                        .getLong(DiaryDto.LAST_MODIFIED_AT) ?: 0L

                    docId to serverUpdatedAt
                }
            }
            .awaitAll()
            .toMap()

        // 동기화 상태별 Batch 작업을 예약
        items.forEach { relations ->
            val entity = relations.diary
            val docRef = diariesRef.document(entity.id)

            when (entity.syncStatus) {
                DiarySyncStatus.PENDING_CREATE -> { batch.set(docRef, relations.toDiaryDto()) }
                DiarySyncStatus.PENDING_UPDATE -> {
                    val serverUpdatedAt = serverUpdatedAts[entity.id] ?: 0L
                    // 클라이언트 데이터가 서버와 같거나 더 최신일 때만 덮어씁니다.
                    if (entity.lastModifiedAt >= serverUpdatedAt) {
                        batch.set(docRef, relations.toDiaryDto())
                    }
                }
                DiarySyncStatus.PENDING_DELETE -> { batch.delete(docRef) }
                DiarySyncStatus.SYNCED -> { /* Do nothing */ }
            }
        }

        runSuspendCatching {
            batch.commit().await()

            // DB 상태를 갱신
            items.forEach { relations ->
                val entity = relations.diary
                when (entity.syncStatus) {
                    DiarySyncStatus.PENDING_DELETE -> {
                        diaryLocalDataSource.delete(entity.id)
                    }
                    DiarySyncStatus.PENDING_CREATE,
                    DiarySyncStatus.PENDING_UPDATE -> {
                        diaryLocalDataSource.markAsSynced(entity.id)
                    }
                    DiarySyncStatus.SYNCED -> { }
                }
            }
        }.mapError { it.toDomainError() }
    }

    private suspend fun pullChunk(userId: String): Result<Boolean, DomainError> {
        val lastPullUpdatedAt = dataStore.getLastPullDiaryModifiedAt()
        val oneYearAgo = serverTime.now() - TimeUnit.DAYS.toMillis(365)

        return runSuspendCatching {
            // 최근 1년치 데이터를 필터링해 50개 수집
            val remoteDocs =
                firebaseFirestoreProvider.getDiariesRef(userId)
                    .whereGreaterThanOrEqualTo(
                        DiaryDto.LAST_MODIFIED_AT,
                        lastPullUpdatedAt
                    )
                    .whereGreaterThanOrEqualTo(DiaryDto.CREATED_AT, oneYearAgo)
                    .orderBy(DiaryDto.LAST_MODIFIED_AT)
                    .limit(CHUNK_SIZE.toLong())
                    .get()
                    .await()

            val remoteDiaries = remoteDocs.documents.mapNotNull { it.toObject(DiaryDto::class.java) }
            val latestTimestamp = pullRemoteDiaries(remoteDiaries)

            if (latestTimestamp > 0) {
                dataStore.setLastPullDiaryModifiedAt(latestTimestamp)
            }

            remoteDocs.size() >= CHUNK_SIZE
        }.mapError { it.toDomainError() }
    }

    // 리얼타임 리스너나 다른 동기화 작업에서 공통으로 호출할 데이터 처리 로직
    internal suspend fun pullRemoteDiaries(remoteDiaries: List<DiaryDto>): Long {
        var maxTimestampInBatch = 0L

        remoteDiaries.forEach { remoteDiary ->
            // 로컬 반영 여부와 상관없이 이 배치의 최대 시각을 추적
            if (remoteDiary.lastModifiedAt > maxTimestampInBatch) {
                maxTimestampInBatch = remoteDiary.lastModifiedAt
            }

            val localEntry = diaryLocalDataSource.getDiary(remoteDiary.id)
            if (localEntry == null || remoteDiary.lastModifiedAt > localEntry.lastModifiedAt) {
                diaryLocalDataSource.upsert(
                    remoteDiary.toDiaryWithRelations(DiarySyncStatus.SYNCED)
                )
            }
        }

        return maxTimestampInBatch
    }

    /**
     * 최근 1년 일기목록을 Firestore에서 가져와 Room에 저장합니다.
     * 청크 단위로 순차적으로 데이터를 가져옵니다.
     *
     * @param userId
     * @param onProgress
     */
    override suspend fun performInitialPull(
        userId: String,
        onProgress: (Float) -> Unit
    ): Result<Unit, DomainError> = runSuspendCatching {
        val oneYearAgo = serverTime.now() - TimeUnit.DAYS.toMillis(365)
        val diariesRef = firebaseFirestoreProvider.getDiariesRef(userId)
        
        // 1. 전체 데이터 수 확인 (진행률 계산용)
        val countSnapshot = diariesRef
            .whereGreaterThanOrEqualTo(DiaryDto.CREATED_AT, oneYearAgo)
            .count()
            .get(AggregateSource.SERVER)
            .await()
        
        val total = countSnapshot.count.toFloat()
        if (total == 0f) {
            dataStore.setLastPullDiaryModifiedAt(serverTime.now())
            return@runSuspendCatching
        }

        var fetchedCount = 0
        var lastVisible: DocumentSnapshot? = null

        while (fetchedCount < total) {
            var query = diariesRef
                .whereGreaterThanOrEqualTo(DiaryDto.CREATED_AT, oneYearAgo)
                .orderBy(DiaryDto.CREATED_AT)
                .limit(CHUNK_SIZE.toLong())

            lastVisible?.let { query = query.startAfter(it) }

            val snapshot = query.get().await()
            if (snapshot.isEmpty) break

            snapshot.documents.forEach { doc ->
                val remoteDiaryDto = doc.toObject(DiaryDto::class.java)
                remoteDiaryDto?.let {
                    diaryLocalDataSource.upsert(
                        it.toDiaryWithRelations(DiarySyncStatus.SYNCED)
                    )
                }
            }

            fetchedCount += snapshot.size()
            lastVisible = snapshot.documents.lastOrNull()
            onProgress(fetchedCount / total)
        }

        // 마지막 문서의 lastModifiedAt 데이터를 저장 (이후 Pull 시 기준점)
        // CreatedAt 기준으로 가져왔으므로, Pull을 위한 lastModifiedAt 기준점은 별도로 확인이 필요할 수 있으나
        // 여기서는 가장 최신 lastModifiedAt을 찾아 저장하는 방식을 고려하거나,
        // 전체 문서 중 최댓값을 찾는 쿼리를 추가할 수 있음. 
        // 일단 성능을 위해 마지막 청크의 문서들 중 최댓값을 사용.
        val lastModifiedAt = diaryLocalDataSource.getLastModifiedAt()
        dataStore.setLastPullDiaryModifiedAt(lastModifiedAt)
        
    }.map { Unit }
    .mapError { it.toDomainError() }

}