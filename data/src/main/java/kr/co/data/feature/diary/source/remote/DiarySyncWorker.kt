package kr.co.data.feature.diary.source.remote

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import kr.co.data.feature.diary.mapper.DiaryDataMapper.toDiaryDto
import kr.co.data.feature.diary.mapper.DiaryDataMapper.toDiaryEntity
import kr.co.data.feature.diary.model.DiaryDto
import kr.co.data.local.dao.DiaryDao
import kr.co.data.local.entity.DiaryEntity
import kr.co.data.local.table.DiaryTable.COLUMN_TIMESTAMP


@HiltWorker
class DiarySyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val diaryDao: DiaryDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : CoroutineWorker(appContext, workerParams) {

    sealed class SyncResult {
        object LocalDataDelete : SyncResult()
        object LocalDataUpload : SyncResult()
        data class RemoteDataDownload(val remoteDiary: DiaryEntity) : SyncResult()
        object Skip : SyncResult()
    }

    companion object {
        const val DIARY_SYNC = "diary_sync"
        private val TAG = DiarySyncWorker::class.java.simpleName
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount > 5) Result.failure()
        val userId = firebaseAuth.currentUser?.uid ?: return Result.failure()
        val unsyncedDiaries = diaryDao.getUnsyncedDiaries() ?: return Result.success()

        return try {
            val localDiaryIdsToDelete = mutableListOf<Long>()
            val localDiaryIdsToSync = mutableListOf<Long>()
            val downloadDiaries = mutableListOf<DiaryEntity>()

            unsyncedDiaries.forEach { diaryEntity ->
                val diaryDocRef =
                    firestore
                        .collection(FirestorePaths.COLLECTION_USERS)
                        .document(userId)
                        .collection(FirestorePaths.COLLECTION_DIARIES)
                        .document(diaryEntity.id.toString())

                val result = firestore.runTransaction { transaction ->
                    val diaryDocSnapshot = transaction.get(diaryDocRef)

                    // 서버에 해당 일기가 없음
                    if (!diaryDocSnapshot.exists()) {
                        if (diaryEntity.isDeleted) {
                            // 로컬에서 삭제된 일기로 -> 로컬 일기만 삭제
                            return@runTransaction SyncResult.LocalDataDelete
                        } else {
                            // 로컬에 일기가 있음 -> 로컬 일기를 업로드
                            val firestoreDiaryDto = diaryEntity.toDiaryDto()
                            transaction.set(diaryDocRef, firestoreDiaryDto)
                            return@runTransaction SyncResult.LocalDataUpload
                        }
                    }

                    // 서버에 해당 일기가 있음
                    val remoteDiaryTimestamp = diaryDocSnapshot.getLong(COLUMN_TIMESTAMP) ?: 0L
                    // 일기 시간 비교
                    if (remoteDiaryTimestamp > diaryEntity.timestamp) {
                        // 서버의 시간이 최신임으로 -> 서버 일기를 다운로드
                        try {
                            val diaryDto = diaryDocSnapshot.toObject(DiaryDto::class.java)
                            if (diaryDto == null) {
                                Log.w(
                                    TAG,
                                    "Failed to parse Firestore document to DiaryDto. Document ID: ${diaryDocSnapshot.id}"
                                )
                                return@runTransaction SyncResult.Skip
                            }
                            val remoteDiary = diaryDto
                                .toDiaryEntity()
                                .copy(isSynced = true, isDeleted = false)
                            return@runTransaction SyncResult.RemoteDataDownload(remoteDiary)
                        } catch (error: IllegalArgumentException) {
                            Log.w(
                                TAG,
                                "Skipping remote diary due to mapping error. Document ID: ${diaryDocSnapshot.id}",
                                error
                            )
                            return@runTransaction SyncResult.Skip
                        }
                    } else {
                        // 로컬의 시간이 최신이지만
                        if (diaryEntity.isDeleted) {
                            // 로컬에서 삭제된 일기로 -> 로컬, 서버 모두 삭제
                            transaction.delete(diaryDocRef)
                            return@runTransaction SyncResult.LocalDataDelete
                        } else {
                            // 로컬에 일기가 있음 -> 로컬 일기를 업로드
                            transaction.set(diaryDocRef, diaryEntity.toDiaryDto())
                            return@runTransaction SyncResult.LocalDataUpload
                        }
                    }
                }.await()

                // DB 처리에 필요한 id, entity 수집.
                when (result) {
                    is SyncResult.LocalDataUpload -> {
                        localDiaryIdsToSync.add(diaryEntity.id)
                    }

                    is SyncResult.LocalDataDelete -> {
                        localDiaryIdsToDelete.add(diaryEntity.id)
                    }

                    is SyncResult.RemoteDataDownload -> {
                        downloadDiaries.add(result.remoteDiary)
                    }

                    is SyncResult.Skip -> {}
                }
            }

            // DB 처리.
            if (localDiaryIdsToSync.isNotEmpty()) {
                diaryDao.updateSyncStatus(localDiaryIdsToSync, true)
            }
            if (localDiaryIdsToDelete.isNotEmpty()) {
                diaryDao.deleteDiariesByIds(localDiaryIdsToDelete)
            }
            if (downloadDiaries.isNotEmpty()) {
                diaryDao.upsertDiaries(downloadDiaries)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount > 5) Result.failure()
            else Result.retry()
        }
    }
}