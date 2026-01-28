package kr.co.data.remote

import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kr.co.data.di.IoScope
import kr.co.data.local.dao.DiaryDAO
import kr.co.data.mapper.DiaryEntityMapper.toDiaryEntity
import kr.co.data.model.diary.FirestoreDiaryDto
import kr.co.data.remote.DiaryDownloadWorker.Companion.WORK_DIARY_DOWNLOAD
import kr.co.data.remote.DiarySyncWorker.Companion.WORK_DIARY_SYNC
import kr.co.data.remote.FirestorePaths.COLLECTION_DIARIES
import kr.co.data.remote.FirestorePaths.COLLECTION_USERS
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class DiaryRemoteDataSource @Inject constructor(
    private val workManager: WorkManager,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val diaryDao: DiaryDAO,
    @IoScope private val ioScope: CoroutineScope
) {
    companion object {
        private val TAG: String = DiaryRemoteDataSource::class.java.simpleName
    }

    private var listenerRegistration: ListenerRegistration? = null

    /**
     * Firestore에 전체 일기 데이터 요청을 스케줄링
     *
     * 초기 로컬 DB 생성을 위해 Firestore의 모든 데이터를 가져오는 스케줄링
     */
    fun scheduleDiaryDownload() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val diaryDownloadWorker = OneTimeWorkRequestBuilder<DiaryDownloadWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL, // 실패시 재시도 시간차가 2배씩 증가: 10초 -> 20초 -> 40초
                10, TimeUnit.SECONDS, // 10초
            )
            .build()

        workManager.enqueueUniqueWork(
            WORK_DIARY_DOWNLOAD,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            diaryDownloadWorker
        )
    }

    /**
     * Firestore에 동기화 요청을 스케줄링
     *
     * Local DB, Firestore간 비동기화된 데이터를 동기화하는 스케줄링
     */
    fun scheduleDiarySync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val diarySyncWorker = OneTimeWorkRequestBuilder<DiarySyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10, TimeUnit.SECONDS, // 10초
            )
            .build()

        workManager.enqueueUniqueWork(
            WORK_DIARY_SYNC,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            diarySyncWorker
        )
    }

    /**
     * 다른 단말기에서 변경이 일어나는 데이터를 받아와서 반영하기 위한 동기화 리스너 등록
     * Firestore 실시간 리스너를 시작하고, 변경사항을 로컬 DB에 반영
     */
    fun startRealtimeSync() {
        if (listenerRegistration != null) return

        val userId = auth.currentUser?.uid ?: return

        val query =
            firestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_DIARIES)

        listenerRegistration = query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e(TAG, "addSnapshotListener listen failed: ${e.message}")
                return@addSnapshotListener
            }

            val changedDiaries = snapshots?.documentChanges?.mapNotNull { change ->
                val firestoreDiaryDto = change.document.toObject(FirestoreDiaryDto::class.java)
                firestoreDiaryDto.toDiaryEntity().copy(isSynced = true, isDeleted = false)
            }

            if (!changedDiaries.isNullOrEmpty()) {
                ioScope.launch {
                    diaryDao.upsertDiaries(changedDiaries)
                }
            }
        }
    }

    /**
     * 리소스 정리를 위해 리스너를 중지
     */
    fun stopRealtimeSync() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}
