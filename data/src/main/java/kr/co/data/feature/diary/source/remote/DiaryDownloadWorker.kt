package kr.co.data.feature.diary.source.remote

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import kr.co.data.feature.diary.mapper.DiaryDataMapper.toDiaryEntity
import kr.co.data.feature.diary.model.DiaryDto
import kr.co.data.local.dao.DiaryDao


@HiltWorker
class DiaryDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val diaryDao: DiaryDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_DIARY_DOWNLOAD = "diary_sync_download"
    }

    override suspend fun doWork(): Result {
        if (runAttemptCount > 5) Result.failure()
        val userId = firebaseAuth.currentUser?.uid ?: return Result.failure()

        try {
            val diariesSnapshot =
                firestore
                    .collection(FirestorePaths.COLLECTION_USERS)
                    .document(userId)
                    .collection(FirestorePaths.COLLECTION_DIARIES)
                    .get()
                    .await()

            val downloadDiaries = diariesSnapshot.documents.mapNotNull { diaryDocSnapshot ->
                val diaryDto = diaryDocSnapshot.toObject(DiaryDto::class.java)!!
                diaryDto.toDiaryEntity().copy(isSynced = true, isDeleted = false)
            }

            if (downloadDiaries.isNotEmpty()) {
                diaryDao.upsertDiaries(downloadDiaries)
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return if (runAttemptCount > 2) Result.failure() else Result.retry()
        }
    }
}