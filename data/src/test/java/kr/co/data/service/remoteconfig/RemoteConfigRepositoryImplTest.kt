package kr.co.data.service.remoteconfig

import com.google.android.gms.tasks.Tasks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.co.core.firebase.provider.FirebaseRemoteConfigProvider
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.fake.FakeAppLogger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RemoteConfigRepositoryImplTest : BaseDataUnitTest() {
    private val logger = FakeAppLogger()
    private val provider = mockk<FirebaseRemoteConfigProvider>()
    private val repository = RemoteConfigRepositoryImpl(
        logger = logger,
        firebaseRemoteConfigProvider = provider,
    )

    @Test
    fun `fetchAndActivate sets minimum fetch interval and returns provider result`() = runDataTest {
        every { provider.setMinimumFetchIntervalInSeconds(any()) } returns Tasks.forResult(null)
        every { provider.fetchAndActivate() } returns Tasks.forResult(true)

        assertTrue(repository.fetchAndActivate())

        verify(exactly = 1) { provider.setMinimumFetchIntervalInSeconds(0L) }
        verify(exactly = 1) { provider.fetchAndActivate() }
    }

    @Test
    fun `fetchAndActivate returns false and logs when provider fails`() = runDataTest {
        every { provider.setMinimumFetchIntervalInSeconds(any()) } returns Tasks.forException(
            IllegalStateException("failure-test"),
        )

        assertFalse(repository.fetchAndActivate())

        assertEquals("Failed to fetchAndActivate", logger.errorThrowables.single().second)
    }

    @Test
    fun `remote config flags delegate to provider keys`() {
        every { provider.getBoolean("is_maintenance_mode") } returns true
        every { provider.getString("maintenance_reason") } returns "maintenance-reason-test"
        every { provider.getBoolean("user_data_sync_enabled") } returns true
        every { provider.getBoolean("profile_photo_upload_enabled") } returns false
        every { provider.getBoolean("diary_sync_enabled") } returns true
        every { provider.getBoolean("ai_enabled") } returns false
        every { provider.getLong("last_modified_at") } returns 100L

        assertTrue(repository.isMaintenanceMode())
        assertEquals("maintenance-reason-test", repository.getMaintenanceReason())
        assertTrue(repository.isUserDataSyncEnabled())
        assertFalse(repository.isProfilePhotoUploadEnabled())
        assertTrue(repository.isDiarySyncEnabled())
        assertFalse(repository.isAiEnabled())
        assertEquals(100L, repository.getLastModifiedAt())
    }
}
