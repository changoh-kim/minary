package kr.co.core.storage.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LocalStoragePathProviderTest {

    private val provider = LocalStoragePathProvider()

    @Test
    fun `returns stable app and user scoped storage names`() {
        assertEquals("minary_datastore", provider.getMinaryDataStoreName())
        assertEquals("user_database_uid-test", provider.getUserDatabaseName("uid-test"))
        assertEquals("user_profile_uid-test.pb", provider.getUserProfileDataStoreName("uid-test"))
        assertEquals("user_settings_uid-test.pb", provider.getUserSettingsDataStoreName("uid-test"))
        assertEquals("user_sync_uid-test.pb", provider.getUserSyncDataStoreName("uid-test"))
        assertEquals("user_dir_uid-test", provider.getUserDirectoryName("uid-test"))
        assertEquals("user_profile_photo_uid-test.jpg", provider.getUserProfilePhotoFileName("uid-test"))
        assertEquals("user_temp_profile_photo_uid-test.jpg", provider.getUserTempProfilePhotoFileName("uid-test"))
    }
}
