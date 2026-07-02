package kr.co.data.feature.setting.sync.worker

import androidx.work.ListenableWorker
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import kr.co.core.common.error.DomainError
import kr.co.core.common.result.AppResult
import kr.co.core.firebase.provider.FirebaseAuthProvider
import kr.co.data.testing.AndroidDataFixtures
import kr.co.data.testing.BaseDataInstrumentationTest
import kr.co.data.testing.workerParameters
import kr.co.domain.feature.setting.sync.UserSettingsSyncManager
import org.junit.Assert.assertEquals
import org.junit.Test

class UserSettingsPushWorkerInstrumentedTest : BaseDataInstrumentationTest() {
    @Test
    fun doWork_returns_failure_when_current_user_is_missing() = runDataAndroidTest {
        val authProvider = mockk<FirebaseAuthProvider> {
            every { currentUser } returns null
        }
        val syncManager = FakeUserSettingsSyncManager()
        val worker = worker(authProvider = authProvider, syncManager = syncManager)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        assertEquals(emptyList<String>(), syncManager.settingsPushUids)
    }

    @Test
    fun doWork_pushes_settings_and_returns_success_when_push_succeeds() = runDataAndroidTest {
        val syncManager = FakeUserSettingsSyncManager(settingsPushResult = Ok(Unit))
        val worker = worker(syncManager = syncManager)

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), syncManager.settingsPushUids)
    }

    @Test
    fun doWork_returns_retry_when_settings_push_fails_before_retry_limit() = runDataAndroidTest {
        val syncManager = FakeUserSettingsSyncManager(
            settingsPushResult = Err(DomainError.NetworkUnavailable),
        )
        val worker = worker(syncManager = syncManager, runAttemptCount = 2)

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), syncManager.settingsPushUids)
    }

    @Test
    fun doWork_returns_failure_when_settings_push_fails_at_retry_limit() = runDataAndroidTest {
        val syncManager = FakeUserSettingsSyncManager(
            settingsPushResult = Err(DomainError.NetworkUnavailable),
        )
        val worker = worker(syncManager = syncManager, runAttemptCount = 3)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), syncManager.settingsPushUids)
    }

    private fun worker(
        authProvider: FirebaseAuthProvider = authProvider(),
        syncManager: UserSettingsSyncManager,
        runAttemptCount: Int = 0,
    ) = UserSettingsPushWorker(
        context = context,
        params = workerParameters(runAttemptCount),
        firebaseAuthProvider = authProvider,
        userSettingsSyncManager = syncManager,
    )

    private fun authProvider(): FirebaseAuthProvider {
        val user = mockk<FirebaseUser> {
            every { uid } returns AndroidDataFixtures.UID
        }
        return mockk {
            every { currentUser } returns user
        }
    }

    private class FakeUserSettingsSyncManager(
        private val settingsPushResult: AppResult<Unit> = Ok(Unit),
    ) : UserSettingsSyncManager {
        val settingsPushUids = mutableListOf<String>()

        override suspend fun syncSettings(userId: String): AppResult<Unit> =
            Ok(Unit)

        override suspend fun pushSettings(userId: String): AppResult<Unit> {
            settingsPushUids += userId
            return settingsPushResult
        }

        override suspend fun pullSettings(userId: String): AppResult<Unit> =
            Ok(Unit)
    }
}
