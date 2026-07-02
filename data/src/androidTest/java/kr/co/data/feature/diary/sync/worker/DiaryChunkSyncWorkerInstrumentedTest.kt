package kr.co.data.feature.diary.sync.worker

import androidx.work.ListenableWorker
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kr.co.core.common.error.DomainError
import kr.co.core.common.model.AppTheme
import kr.co.core.common.result.AppResult
import kr.co.core.common.state.SyncStatus
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.data.testing.AndroidDataFixtures
import kr.co.data.testing.BaseDataInstrumentationTest
import kr.co.data.testing.workerParameters
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.diary.sync.DiarySyncManager
import kr.co.domain.feature.diary.sync.DiarySyncScheduler
import kr.co.domain.feature.setting.model.UserSettings
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.service.time.ServerTimeProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DiaryChunkSyncWorkerInstrumentedTest : BaseDataInstrumentationTest() {
    @Test
    fun fullDoWork_returns_failure_when_current_user_is_missing() = runDataAndroidTest {
        val deps = WorkerDeps(authProvider = authProvider(userId = null))
        val worker = fullWorker(deps)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        assertEquals(0, deps.serverTime.syncCallCount)
        assertEquals(emptyList<String>(), deps.syncManager.chunkedSyncUids)
    }

    @Test
    fun fullDoWork_returns_success_without_chunk_sync_when_diary_sync_is_disabled() =
        runDataAndroidTest {
            val deps = WorkerDeps(userSettingsRepository = FakeUserSettingsRepository(false))
            val worker = fullWorker(deps)

            assertEquals(ListenableWorker.Result.success(), worker.doWork())
            assertEquals(1, deps.serverTime.syncCallCount)
            assertEquals(emptyList<String>(), deps.syncManager.chunkedSyncUids)
            assertEquals(0, deps.diaryRepository.deleteOldDiariesCallCount)
            assertEquals(0, deps.scheduler.rescheduleFullSyncCallCount)
        }

    @Test
    fun fullDoWork_reschedules_full_sync_when_chunk_sync_has_more_work() = runDataAndroidTest {
        val deps = WorkerDeps(
            syncManager = FakeDiarySyncManager(chunkedSyncResult = Ok(true)),
        )
        val worker = fullWorker(deps)

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), deps.syncManager.chunkedSyncUids)
        assertEquals(1, deps.scheduler.rescheduleFullSyncCallCount)
        assertEquals(0, deps.diaryRepository.deleteOldDiariesCallCount)
    }

    @Test
    fun fullDoWork_deletes_old_diaries_when_chunk_sync_is_complete() = runDataAndroidTest {
        val deps = WorkerDeps(
            syncManager = FakeDiarySyncManager(chunkedSyncResult = Ok(false)),
        )
        val worker = fullWorker(deps)

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), deps.syncManager.chunkedSyncUids)
        assertEquals(0, deps.scheduler.rescheduleFullSyncCallCount)
        assertEquals(1, deps.diaryRepository.deleteOldDiariesCallCount)
    }

    @Test
    fun fullDoWork_returns_retry_when_chunk_sync_fails_before_retry_limit() = runDataAndroidTest {
        val deps = WorkerDeps(
            syncManager = FakeDiarySyncManager(
                chunkedSyncResult = Err(DomainError.NetworkUnavailable),
            ),
        )
        val worker = fullWorker(deps, runAttemptCount = 2)

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
        assertEquals(0, deps.scheduler.rescheduleFullSyncCallCount)
        assertEquals(0, deps.diaryRepository.deleteOldDiariesCallCount)
    }

    @Test
    fun fullDoWork_returns_failure_when_chunk_sync_fails_at_retry_limit() = runDataAndroidTest {
        val deps = WorkerDeps(
            syncManager = FakeDiarySyncManager(
                chunkedSyncResult = Err(DomainError.NetworkUnavailable),
            ),
        )
        val worker = fullWorker(deps, runAttemptCount = 3)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        assertEquals(0, deps.scheduler.rescheduleFullSyncCallCount)
        assertEquals(0, deps.diaryRepository.deleteOldDiariesCallCount)
    }

    @Test
    fun periodicDoWork_returns_failure_when_current_user_is_missing() = runDataAndroidTest {
        val deps = WorkerDeps(authProvider = authProvider(userId = null))
        val worker = periodicWorker(deps)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        assertEquals(0, deps.serverTime.syncCallCount)
        assertEquals(emptyList<String>(), deps.syncManager.chunkedSyncUids)
    }

    @Test
    fun periodicDoWork_returns_success_without_chunk_sync_when_diary_sync_is_disabled() =
        runDataAndroidTest {
            val deps = WorkerDeps(userSettingsRepository = FakeUserSettingsRepository(false))
            val worker = periodicWorker(deps)

            assertEquals(ListenableWorker.Result.success(), worker.doWork())
            assertEquals(1, deps.serverTime.syncCallCount)
            assertEquals(emptyList<String>(), deps.syncManager.chunkedSyncUids)
            assertEquals(0, deps.diaryRepository.deleteOldDiariesCallCount)
            assertEquals(0, deps.scheduler.rescheduleFullSyncCallCount)
        }

    @Test
    fun periodicDoWork_reschedules_full_sync_when_chunk_sync_has_more_work() =
        runDataAndroidTest {
            val deps = WorkerDeps(
                syncManager = FakeDiarySyncManager(chunkedSyncResult = Ok(true)),
            )
            val worker = periodicWorker(deps)

            assertEquals(ListenableWorker.Result.success(), worker.doWork())
            assertEquals(listOf(AndroidDataFixtures.UID), deps.syncManager.chunkedSyncUids)
            assertEquals(1, deps.scheduler.rescheduleFullSyncCallCount)
            assertEquals(0, deps.diaryRepository.deleteOldDiariesCallCount)
        }

    @Test
    fun periodicDoWork_deletes_old_diaries_when_chunk_sync_is_complete() = runDataAndroidTest {
        val deps = WorkerDeps(
            syncManager = FakeDiarySyncManager(chunkedSyncResult = Ok(false)),
        )
        val worker = periodicWorker(deps)

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), deps.syncManager.chunkedSyncUids)
        assertEquals(0, deps.scheduler.rescheduleFullSyncCallCount)
        assertEquals(1, deps.diaryRepository.deleteOldDiariesCallCount)
    }

    @Test
    fun periodicDoWork_returns_retry_when_chunk_sync_fails_before_retry_limit() =
        runDataAndroidTest {
            val deps = WorkerDeps(
                syncManager = FakeDiarySyncManager(
                    chunkedSyncResult = Err(DomainError.NetworkUnavailable),
                ),
            )
            val worker = periodicWorker(deps, runAttemptCount = 2)

            assertEquals(ListenableWorker.Result.retry(), worker.doWork())
            assertEquals(0, deps.scheduler.rescheduleFullSyncCallCount)
            assertEquals(0, deps.diaryRepository.deleteOldDiariesCallCount)
        }

    @Test
    fun periodicDoWork_returns_failure_when_chunk_sync_fails_at_retry_limit() =
        runDataAndroidTest {
            val deps = WorkerDeps(
                syncManager = FakeDiarySyncManager(
                    chunkedSyncResult = Err(DomainError.NetworkUnavailable),
                ),
            )
            val worker = periodicWorker(deps, runAttemptCount = 3)

            assertEquals(ListenableWorker.Result.failure(), worker.doWork())
            assertEquals(0, deps.scheduler.rescheduleFullSyncCallCount)
            assertEquals(0, deps.diaryRepository.deleteOldDiariesCallCount)
        }

    private fun fullWorker(
        deps: WorkerDeps,
        runAttemptCount: Int = 0,
    ) = DiaryFullSyncWorker(
        context = context,
        params = workerParameters(runAttemptCount),
        firebaseAuthProvider = deps.authProvider,
        serverTime = deps.serverTime,
        diaryRepository = deps.diaryRepository,
        userSettingsRepository = deps.userSettingsRepository,
        diarySyncManager = deps.syncManager,
        diarySyncScheduler = deps.scheduler,
    )

    private fun periodicWorker(
        deps: WorkerDeps,
        runAttemptCount: Int = 0,
    ) = DiaryPeriodicSyncWorker(
        context = context,
        params = workerParameters(runAttemptCount),
        firebaseAuthProvider = deps.authProvider,
        serverTime = deps.serverTime,
        diaryRepository = deps.diaryRepository,
        userSettingsRepository = deps.userSettingsRepository,
        diarySyncManager = deps.syncManager,
        diarySyncScheduler = deps.scheduler,
    )

    private fun authProvider(userId: String? = AndroidDataFixtures.UID): FirebaseAuthProvider {
        val user = userId?.let { uidValue ->
            mockk<FirebaseUser> {
                every { uid } returns uidValue
            }
        }
        return mockk {
            every { currentUser } returns user
        }
    }

    private inner class WorkerDeps(
        val authProvider: FirebaseAuthProvider = authProvider(),
        val serverTime: FakeServerTimeProvider = FakeServerTimeProvider(),
        val diaryRepository: FakeDiaryRepository = FakeDiaryRepository(),
        val userSettingsRepository: FakeUserSettingsRepository = FakeUserSettingsRepository(true),
        val syncManager: FakeDiarySyncManager = FakeDiarySyncManager(),
        val scheduler: FakeDiarySyncScheduler = FakeDiarySyncScheduler(),
    )

    private class FakeServerTimeProvider(
        private val syncResult: AppResult<Unit> = Ok(Unit),
    ) : ServerTimeProvider {
        var syncCallCount = 0

        override suspend fun sync(): AppResult<Unit> {
            syncCallCount += 1
            return syncResult
        }

        override fun now(): Long = AndroidDataFixtures.UPDATED_AT
    }

    private class FakeDiaryRepository : DiaryRepository {
        var deleteOldDiariesCallCount = 0

        override fun getSyncStatusStream(yearMonth: YearMonth): Flow<SyncStatus> =
            flowOf(SyncStatus.IDLE)

        override suspend fun requestMonthSync(
            userId: String,
            yearMonth: YearMonth,
        ): AppResult<Unit> = Ok(Unit)

        override suspend fun createDiary(diary: Diary): AppResult<Unit> = Ok(Unit)

        override suspend fun updateDiary(diary: Diary): AppResult<Diary> = Ok(diary)

        override suspend fun deleteDiary(diary: Diary): AppResult<Unit> = Ok(Unit)

        override suspend fun deleteOldDiaries(): AppResult<Unit> {
            deleteOldDiariesCallCount += 1
            return Ok(Unit)
        }

        override suspend fun getDiary(date: LocalDate): AppResult<Diary?> = Ok(null)

        override fun getDiaryStream(date: LocalDate): Flow<Diary?> = flowOf(null)

        override fun getDiariesByDateRangeStream(
            startDate: LocalDate,
            endDate: LocalDate,
        ): Flow<List<Diary>> = flowOf(emptyList())

        override suspend fun getDiariesByDateRange(
            startDate: LocalDate,
            endDate: LocalDate,
        ): AppResult<List<Diary>> = Ok(emptyList())

        override suspend fun getPagedDiaries(
            query: String?,
            startDate: LocalDate?,
            endDate: LocalDate?,
            limit: Int,
            offset: Int,
        ): AppResult<List<Diary>> = Ok(emptyList())

        override val diaryChangeEvent: Flow<Unit> = emptyFlow()
    }

    private class FakeUserSettingsRepository(
        private val diarySyncEnabled: Boolean,
    ) : UserSettingsRepository {
        override suspend fun getUserSettingsStream(): Flow<AppResult<UserSettings>> =
            flowOf(Ok(UserSettings(diarySyncEnabled = diarySyncEnabled)))

        override fun getAppThemeStream(): Flow<AppTheme> = flowOf(AppTheme.SYSTEM)

        override suspend fun updateAppTheme(
            uid: String,
            appTheme: AppTheme,
            lastModifiedAt: Long,
        ): AppResult<Unit> = Ok(Unit)

        override fun getDiarySyncEnabledStream(): Flow<Boolean> =
            flowOf(diarySyncEnabled)

        override suspend fun updateDiarySyncEnabled(
            uid: String,
            enabled: Boolean,
            lastModifiedAt: Long,
        ): AppResult<Unit> = Ok(Unit)
    }

    private class FakeDiarySyncManager(
        private val chunkedSyncResult: AppResult<Boolean> = Ok(false),
    ) : DiarySyncManager {
        val chunkedSyncUids = mutableListOf<String>()

        override suspend fun performChunkedSync(userId: String): AppResult<Boolean> {
            chunkedSyncUids += userId
            return chunkedSyncResult
        }

        override suspend fun performImmediatePush(userId: String): AppResult<Unit> =
            Ok(Unit)

        override suspend fun performMonthSync(
            userId: String,
            yearMonth: YearMonth,
        ): AppResult<Unit> = Ok(Unit)

        override suspend fun performInitialPull(
            userId: String,
            onProgress: (Float) -> Unit,
        ): AppResult<Unit> = Ok(Unit)
    }

    private class FakeDiarySyncScheduler : DiarySyncScheduler {
        var rescheduleFullSyncCallCount = 0

        override fun scheduleFullSync() = Unit

        override fun rescheduleFullSync() {
            rescheduleFullSyncCallCount += 1
        }

        override fun scheduleImmediateSync() = Unit

        override fun schedulePeriodicSync() = Unit

        override fun cancelFullSync() = Unit

        override fun cancelImmediateSync() = Unit

        override fun cancelPeriodicSync() = Unit

        override fun cancelAllSync() = Unit
    }
}
