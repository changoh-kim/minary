package kr.co.data.feature.user.source.local

import android.content.Context
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import kr.co.core.database.provider.UserDatabaseProvider
import kr.co.core.datastore.profile.UserProfileDataStoreProvider
import kr.co.core.datastore.settings.UserSettingsDataStoreProvider
import kr.co.core.datastore.sync.UserSyncDataStoreProvider
import kr.co.core.firebase.provider.FirebaseStorageProvider
import kr.co.core.storage.config.LocalStoragePathProvider
import kr.co.core.storage.provider.UserInternalStorageProvider
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.fake.FakeAppLogger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File

class UserStorageLocalDataSourceTest : BaseDataUnitTest() {
    @Test
    fun `deleteUserStorage deletes database datastores and internal directory for uid`() {
        val harness = harness()

        harness.source.deleteUserStorage(DataFixtures.UID)

        verify(exactly = 1) { harness.userDatabaseProvider.deleteDatabaseFile(DataFixtures.UID) }
        verify(exactly = 1) { harness.userProfileDataStoreProvider.deleteDataStoreFile(DataFixtures.UID) }
        verify(exactly = 1) { harness.userSettingsDataStoreProvider.deleteDataStoreFile(DataFixtures.UID) }
        verify(exactly = 1) { harness.userSyncDataStoreProvider.deleteDataStoreFile(DataFixtures.UID) }
        verify(exactly = 1) { harness.userInternalStorageProvider.deleteUserDirectory(DataFixtures.UID) }
    }

    @Test
    fun `profile photo paths use user directory cache directory and path provider names`() {
        val harness = harness()

        assertEquals(
            File(harness.userDirectory, "profile-photo-test.jpg").absolutePath,
            harness.source.getUserProfilePhotoFilePath(DataFixtures.UID),
        )
        assertEquals(
            File(harness.cacheDirectory, "temp-profile-photo-test.jpg").absolutePath,
            harness.source.getTemporaryProfilePhotoFilePath(DataFixtures.UID),
        )
    }

    @Test
    fun `downloadUserProfilePhoto returns null and logs when storage download fails`() = runDataTest {
        val logger = FakeAppLogger()
        val failure = IllegalStateException("failure-test")
        val harness = harness(logger = logger) {
            every { firebaseStorageProvider.getUserProfilePhotoRef(DataFixtures.UID) } throws failure
        }

        val actual = harness.source.downloadUserProfilePhoto(
            uid = DataFixtures.UID,
            downloadUrl = DataFixtures.PROFILE_PHOTO_URL,
        )

        assertNull(actual)
        assertEquals(failure, logger.errorThrowables.single().first)
        assertEquals("Failed to download user profile photo", logger.errorThrowables.single().second)
    }

    private fun harness(
        logger: FakeAppLogger = FakeAppLogger(),
        extraStubs: StorageHarness.() -> Unit = {},
    ): StorageHarness {
        val userDirectory = File("/tmp/minary-user-dir-test")
        val cacheDirectory = File("/tmp/minary-cache-dir-test")
        val context = mockk<Context> {
            every { cacheDir } returns cacheDirectory
        }
        val userDatabaseProvider = mockk<UserDatabaseProvider> {
            every { deleteDatabaseFile(any()) } just Runs
        }
        val userProfileDataStoreProvider = mockk<UserProfileDataStoreProvider> {
            every { deleteDataStoreFile(any()) } just Runs
        }
        val userSettingsDataStoreProvider = mockk<UserSettingsDataStoreProvider> {
            every { deleteDataStoreFile(any()) } just Runs
        }
        val userSyncDataStoreProvider = mockk<UserSyncDataStoreProvider> {
            every { deleteDataStoreFile(any()) } just Runs
        }
        val userInternalStorageProvider = mockk<UserInternalStorageProvider> {
            every { getUserDirectory() } returns userDirectory
            every { deleteUserDirectory(any()) } just Runs
        }
        val pathProvider = mockk<LocalStoragePathProvider> {
            every { getUserProfilePhotoFileName(DataFixtures.UID) } returns "profile-photo-test.jpg"
            every { getUserTempProfilePhotoFileName(DataFixtures.UID) } returns "temp-profile-photo-test.jpg"
        }
        val firebaseStorageProvider = mockk<FirebaseStorageProvider>()

        return StorageHarness(
            logger = logger,
            context = context,
            userDatabaseProvider = userDatabaseProvider,
            userProfileDataStoreProvider = userProfileDataStoreProvider,
            userSettingsDataStoreProvider = userSettingsDataStoreProvider,
            userSyncDataStoreProvider = userSyncDataStoreProvider,
            userInternalStorageProvider = userInternalStorageProvider,
            pathProvider = pathProvider,
            firebaseStorageProvider = firebaseStorageProvider,
            userDirectory = userDirectory,
            cacheDirectory = cacheDirectory,
        ).apply(extraStubs)
    }

    private data class StorageHarness(
        val logger: FakeAppLogger,
        val context: Context,
        val userDatabaseProvider: UserDatabaseProvider,
        val userProfileDataStoreProvider: UserProfileDataStoreProvider,
        val userSettingsDataStoreProvider: UserSettingsDataStoreProvider,
        val userSyncDataStoreProvider: UserSyncDataStoreProvider,
        val userInternalStorageProvider: UserInternalStorageProvider,
        val pathProvider: LocalStoragePathProvider,
        val firebaseStorageProvider: FirebaseStorageProvider,
        val userDirectory: File,
        val cacheDirectory: File,
    ) {
        val source = UserStorageLocalDataSource(
            logger = logger,
            context = context,
            userDatabaseProvider = userDatabaseProvider,
            userProfileDataStoreProvider = userProfileDataStoreProvider,
            userSettingsDataStoreProvider = userSettingsDataStoreProvider,
            userSyncDataStoreProvider = userSyncDataStoreProvider,
            userInternalStorageProvider = userInternalStorageProvider,
            pathProvider = pathProvider,
            firebaseStorageProvider = firebaseStorageProvider,
        )
    }
}
