package kr.co.data.feature.profile.sync.worker

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
import kr.co.domain.feature.profile.sync.UserProfileSyncManager
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfilePushWorkerInstrumentedTest : BaseDataInstrumentationTest() {
    @Test
    fun doWork_returns_failure_when_current_user_is_missing() = runDataAndroidTest {
        val authProvider = mockk<FirebaseAuthProvider> {
            every { currentUser } returns null
        }
        val syncManager = FakeUserProfileSyncManager()
        val worker = profileWorker(authProvider = authProvider, syncManager = syncManager)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        assertEquals(emptyList<String>(), syncManager.profilePushUids)
    }

    @Test
    fun doWork_pushes_profile_and_returns_success_when_push_succeeds() = runDataAndroidTest {
        val syncManager = FakeUserProfileSyncManager(profilePushResult = Ok(Unit))
        val worker = profileWorker(syncManager = syncManager)

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), syncManager.profilePushUids)
    }

    @Test
    fun doWork_returns_retry_when_profile_push_fails_before_retry_limit() = runDataAndroidTest {
        val syncManager = FakeUserProfileSyncManager(
            profilePushResult = Err(DomainError.NetworkUnavailable),
        )
        val worker = profileWorker(syncManager = syncManager, runAttemptCount = 2)

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), syncManager.profilePushUids)
    }

    @Test
    fun doWork_returns_failure_when_profile_push_fails_at_retry_limit() = runDataAndroidTest {
        val syncManager = FakeUserProfileSyncManager(
            profilePushResult = Err(DomainError.NetworkUnavailable),
        )
        val worker = profileWorker(syncManager = syncManager, runAttemptCount = 3)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), syncManager.profilePushUids)
    }

    @Test
    fun photoDoWork_returns_failure_when_current_user_is_missing() = runDataAndroidTest {
        val authProvider = mockk<FirebaseAuthProvider> {
            every { currentUser } returns null
        }
        val syncManager = FakeUserProfileSyncManager()
        val worker = photoWorker(authProvider = authProvider, syncManager = syncManager)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        assertEquals(emptyList<String>(), syncManager.photoPushUids)
    }

    @Test
    fun photoDoWork_pushes_photo_and_returns_success_when_push_succeeds() = runDataAndroidTest {
        val syncManager = FakeUserProfileSyncManager(photoPushResult = Ok(Unit))
        val worker = photoWorker(syncManager = syncManager)

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), syncManager.photoPushUids)
    }

    @Test
    fun photoDoWork_returns_retry_when_photo_push_fails_before_retry_limit() = runDataAndroidTest {
        val syncManager = FakeUserProfileSyncManager(
            photoPushResult = Err(DomainError.NetworkUnavailable),
        )
        val worker = photoWorker(syncManager = syncManager, runAttemptCount = 2)

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), syncManager.photoPushUids)
    }

    @Test
    fun photoDoWork_returns_failure_when_photo_push_fails_at_retry_limit() = runDataAndroidTest {
        val syncManager = FakeUserProfileSyncManager(
            photoPushResult = Err(DomainError.NetworkUnavailable),
        )
        val worker = photoWorker(syncManager = syncManager, runAttemptCount = 3)

        assertEquals(ListenableWorker.Result.failure(), worker.doWork())
        assertEquals(listOf(AndroidDataFixtures.UID), syncManager.photoPushUids)
    }

    private fun profileWorker(
        authProvider: FirebaseAuthProvider = authProvider(),
        syncManager: UserProfileSyncManager,
        runAttemptCount: Int = 0,
    ) = UserProfilePushWorker(
        context = context,
        params = workerParameters(runAttemptCount),
        firebaseAuthProvider = authProvider,
        userProfileSyncManager = syncManager,
    )

    private fun photoWorker(
        authProvider: FirebaseAuthProvider = authProvider(),
        syncManager: UserProfileSyncManager,
        runAttemptCount: Int = 0,
    ) = UserProfilePhotoPushWorker(
        context = context,
        params = workerParameters(runAttemptCount),
        firebaseAuthProvider = authProvider,
        userProfileSyncManager = syncManager,
    )

    private fun authProvider(): FirebaseAuthProvider {
        val user = mockk<FirebaseUser> {
            every { uid } returns AndroidDataFixtures.UID
        }
        return mockk {
            every { currentUser } returns user
        }
    }

    private class FakeUserProfileSyncManager(
        private val profilePushResult: AppResult<Unit> = Ok(Unit),
        private val photoPushResult: AppResult<Unit> = Ok(Unit),
    ) : UserProfileSyncManager {
        val profilePushUids = mutableListOf<String>()
        val photoPushUids = mutableListOf<String>()

        override suspend fun syncProfile(userId: String): AppResult<Unit> =
            Ok(Unit)

        override suspend fun pushProfile(userId: String): AppResult<Unit> {
            profilePushUids += userId
            return profilePushResult
        }

        override suspend fun pullProfile(userId: String): AppResult<Unit> =
            Ok(Unit)

        override suspend fun pushProfilePhoto(userId: String): AppResult<Unit> {
            photoPushUids += userId
            return photoPushResult
        }
    }
}
