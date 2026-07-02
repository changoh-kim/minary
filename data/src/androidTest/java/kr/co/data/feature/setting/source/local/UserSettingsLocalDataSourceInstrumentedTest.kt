package kr.co.data.feature.setting.source.local

import androidx.datastore.core.DataStoreFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kr.co.core.datastore.proto.ThemeProto
import kr.co.core.datastore.proto.UserSettingsProto
import kr.co.core.datastore.settings.UserSettingsDataStoreProvider
import kr.co.core.datastore.settings.UserSettingsSerializer
import kr.co.data.testing.AndroidDataFixtures
import kr.co.data.testing.AndroidFakeAppLogger
import kr.co.data.testing.BaseDataInstrumentationTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class UserSettingsLocalDataSourceInstrumentedTest : BaseDataInstrumentationTest() {
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var dataStoreFile: File
    private lateinit var source: UserSettingsLocalDataSource

    @Before
    fun setUpDataStore() {
        dataStoreScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        dataStoreFile = File(
            context.cacheDir,
            "user-settings-local-source-${System.nanoTime()}.pb",
        )
        val dataStore = DataStoreFactory.create(
            serializer = UserSettingsSerializer(),
            scope = dataStoreScope,
            produceFile = { dataStoreFile },
        )
        val provider = mockk<UserSettingsDataStoreProvider>()
        every { provider.getDataStore() } returns dataStore
        source = UserSettingsLocalDataSource(
            logger = AndroidFakeAppLogger(),
            provider = provider,
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
    fun getUserSettings_returns_default_proto_before_update() = runDataAndroidTest {
        assertEquals(UserSettingsProto.getDefaultInstance(), source.getUserSettings())
    }

    @Test
    fun updateUserSettings_persists_settings_and_updates_projection_flows() = runDataAndroidTest {
        source.updateUserSettings(AndroidDataFixtures.userSettingsProto)

        assertEquals(AndroidDataFixtures.userSettingsProto, source.getUserSettings())
        assertEquals(ThemeProto.DARK, source.getAppThemeFlow().first())
        assertEquals(true, source.getDiarySyncEnabledFlow().first())
    }
}
