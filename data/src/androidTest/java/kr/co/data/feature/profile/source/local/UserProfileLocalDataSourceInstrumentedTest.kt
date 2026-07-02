package kr.co.data.feature.profile.source.local

import androidx.datastore.core.DataStoreFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kr.co.core.datastore.profile.UserProfileDataStoreProvider
import kr.co.core.datastore.profile.UserProfileSerializer
import kr.co.core.datastore.proto.UserProfileProto
import kr.co.data.feature.user.source.local.UserStorageLocalDataSource
import kr.co.data.testing.AndroidDataFixtures
import kr.co.data.testing.AndroidFakeAppLogger
import kr.co.data.testing.BaseDataInstrumentationTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class UserProfileLocalDataSourceInstrumentedTest : BaseDataInstrumentationTest() {
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStoreFile: File
    private lateinit var source: UserProfileLocalDataSource

    @Before
    fun setUpDataStore() {
        dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        dataStoreFile = File(
            context.cacheDir,
            "user-profile-local-source-${System.nanoTime()}.pb",
        )
        val dataStore = DataStoreFactory.create(
            serializer = UserProfileSerializer(),
            scope = dataStoreScope,
            produceFile = { dataStoreFile },
        )
        val provider = mockk<UserProfileDataStoreProvider>()
        every { provider.getDataStore() } returns dataStore
        val userStorageLocalDataSource = mockk<UserStorageLocalDataSource>()
        every {
            userStorageLocalDataSource.getUserProfilePhotoFilePath(AndroidDataFixtures.UID)
        } returns "/tmp/profile-photo-test.jpg"
        source = UserProfileLocalDataSource(
            logger = AndroidFakeAppLogger(),
            userProfileDataStoreProvider = provider,
            userStorageLocalDataSource = userStorageLocalDataSource,
        )
    }

    @After
    fun tearDownDataStore() {
        if (::dataStoreScope.isInitialized) {
            dataStoreScope.cancel()
        }
        if (::dataStoreFile.isInitialized) {
            dataStoreFile.delete()
        }
    }

    @Test
    fun getUserProfile_returns_default_proto_before_update() = runDataAndroidTest {
        assertEquals(UserProfileProto.getDefaultInstance(), source.getUserProfile())
    }

    @Test
    fun updateUserProfile_persists_profile_and_updates_flow() = runDataAndroidTest {
        source.updateUserProfile(AndroidDataFixtures.userProfileProto)

        assertEquals(AndroidDataFixtures.userProfileProto, source.getUserProfile())
        assertEquals(AndroidDataFixtures.userProfileProto, source.getUserProfileFlow().first())
    }

    @Test
    fun updateUserProfilePhotoUrl_updates_only_photo_fields() = runDataAndroidTest {
        source.updateUserProfile(AndroidDataFixtures.userProfileProto)

        source.updateUserProfilePhotoUrl(
            profilePhotoUrl = "updated-profile-photo-url-test",
            lastModifiedAt = 500L,
        )

        val actual = source.getUserProfile()
        assertEquals(AndroidDataFixtures.UID, actual.uid)
        assertEquals("updated-profile-photo-url-test", actual.profilePhotoUrl)
        assertEquals(500L, actual.lastModifiedAt)
    }

    @Test
    fun getProfilePhotoFilePath_delegates_to_user_storage_source() {
        assertEquals(
            "/tmp/profile-photo-test.jpg",
            source.getProfilePhotoFilePath(AndroidDataFixtures.UID),
        )
    }
}
