package kr.co.data.feature.diary.sync.worker

import androidx.work.ListenableWorker
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kr.co.core.common.error.DomainError
import kr.co.core.common.result.AppResult
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.data.testing.AndroidDataFixtures
import kr.co.data.testing.BaseDataInstrumentationTest
import kr.co.data.testing.workerParameters
import kr.co.domain.feature.diary.sync.DiarySyncManager
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class DiaryImmediateSyncWorkerInstrumentedTest : BaseDataInstrumentationTest() {
    @Test
    fun doWork_returns_failure_when_current_user_is_missing() = runDataAndroidTest {
        val authProvider = mockk<FirebaseAuthProvider>()
        every { authProvider.currentUser } returns null
        val syncManager = FakeDiarySyncManager()
        val worker = worker(
            authProvider = authProvider,
            userSettingsRepository = userSettingsRepository(syncEnabled = true),
            syncManager = syncManager,
        )

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        assertEquals(emptyList<String>(), syncManager.immediatePushUids)
    }

    @Test
    fun doWork_returns_success_without_push_when_diary_sync_is_disabled() = runDataAndroidTest {
        val syncManager = FakeDiarySyncManager()
        val worker = worker(
            authProvider = authProvider(),
            userSettingsRepository = userSettingsRepository(syncEnabled = false),
            syncManager = syncManager,
        )

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(emptyList<String>(), syncManager.immediatePushUids)
    }

    @Test
    fun doWork_pushes_current_user_and_returns_success_when_push_succeeds() = runDataAndroidTest {
        val syncManager = FakeDiarySyncManager(immediatePushResult = Ok(Unit))
        val worker = worker(
            authProvider = authProvider(),
            userSettingsRepository = userSettingsRepository(syncEnabled = true),
            syncManager = syncManager,
        )

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), syncManager.immediatePushUids)
    }

    @Test
    fun doWork_returns_retry_when_push_fails_before_retry_limit() = runDataAndroidTest {
        val syncManager = FakeDiarySyncManager(
            immediatePushResult = Err(DomainError.NetworkUnavailable),
        )
        val worker = worker(
            authProvider = authProvider(),
            userSettingsRepository = userSettingsRepository(syncEnabled = true),
            syncManager = syncManager,
            runAttemptCount = 2,
        )

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
    }

    @Test
    fun doWork_returns_failure_when_push_fails_at_retry_limit() = runDataAndroidTest {
        val syncManager = FakeDiarySyncManager(
            immediatePushResult = Err(DomainError.NetworkUnavailable),
        )
        val worker = worker(
            authProvider = authProvider(),
            userSettingsRepository = userSettingsRepository(syncEnabled = true),
            syncManager = syncManager,
            runAttemptCount = 3,
        )

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
    }

    private fun worker(
        authProvider: FirebaseAuthProvider,
        userSettingsRepository: UserSettingsRepository,
        syncManager: DiarySyncManager,
        runAttemptCount: Int = 0,
    ) = DiaryImmediateSyncWorker(
        context = context,
        params = workerParameters(runAttemptCount),
        firebaseAuthProvider = authProvider,
        userSettingsRepository = userSettingsRepository,
        diarySyncManager = syncManager,
    )

    private fun authProvider(): FirebaseAuthProvider {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns AndroidDataFixtures.UID
        return mockk {
            every { currentUser } returns user
        }
    }

    private fun userSettingsRepository(syncEnabled: Boolean): UserSettingsRepository =
        mockk {
            every { getDiarySyncEnabledStream() } returns flowOf(syncEnabled)
        }

    private class FakeDiarySyncManager(
        private val immediatePushResult: AppResult<Unit> = Ok(Unit),
    ) : DiarySyncManager {
        val immediatePushUids = mutableListOf<String>()

        override suspend fun performChunkedSync(userId: String): AppResult<Boolean> =
            Ok(false)

        override suspend fun performImmediatePush(userId: String): AppResult<Unit> {
            immediatePushUids += userId
            return immediatePushResult
        }

        override suspend fun performMonthSync(
            userId: String,
            yearMonth: YearMonth,
        ): AppResult<Unit> = Ok(Unit)

        override suspend fun performInitialPull(
            userId: String,
            onProgress: (Float) -> Unit,
        ): AppResult<Unit> = Ok(Unit)
    }
}
