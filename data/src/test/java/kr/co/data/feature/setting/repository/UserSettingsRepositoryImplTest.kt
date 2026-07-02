package kr.co.data.feature.setting.repository

import kotlinx.coroutines.flow.first
import kr.co.core.common.error.DomainError
import kr.co.core.common.model.AppTheme
import kr.co.core.datastore.proto.ThemeProto
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toUserSettings
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.assertErr
import kr.co.data.testing.assertOk
import kr.co.data.testing.fake.FakeAppLogger
import kr.co.data.testing.fake.FakeUserSettingsLocalDataSource
import kr.co.data.testing.fake.FakeUserSettingsSyncScheduler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserSettingsRepositoryImplTest : BaseDataUnitTest() {
    @Test
    fun `getUserSettingsStream maps local settings to AppResult Ok`() = runDataTest {
        val source = FakeUserSettingsLocalDataSource().apply {
            settingsState.value = DataFixtures.userSettingsProto
        }
        val repository = repository(source = source)

        repository.getUserSettingsStream().first().assertOk(DataFixtures.userSettings)
    }

    @Test
    fun `getAppThemeStream maps proto theme to app theme`() = runDataTest {
        val source = FakeUserSettingsLocalDataSource().apply {
            settingsState.value = DataFixtures.userSettingsProto
        }
        val repository = repository(source = source)

        assertEquals(AppTheme.DARK, repository.getAppThemeStream().first())
    }

    @Test
    fun `updateAppTheme updates local settings and schedules push`() = runDataTest {
        val source = FakeUserSettingsLocalDataSource().apply {
            settingsState.value = DataFixtures.userSettingsProto
        }
        val scheduler = FakeUserSettingsSyncScheduler()
        val repository = repository(source = source, scheduler = scheduler)

        repository.updateAppTheme(DataFixtures.UID, AppTheme.LIGHT, 300L).assertOk(Unit)

        val updated = source.updatedSettings.single()
        assertEquals(ThemeProto.LIGHT, updated.theme)
        assertEquals(300L, updated.lastModifiedAt)
        assertEquals(listOf("scheduleSettingsPush"), scheduler.calls)
    }

    @Test
    fun `getDiarySyncEnabledStream returns local flow value`() = runDataTest {
        val source = FakeUserSettingsLocalDataSource().apply {
            settingsState.value = DataFixtures.userSettingsProto
        }
        val repository = repository(source = source)

        assertEquals(true, repository.getDiarySyncEnabledStream().first())
    }

    @Test
    fun `updateDiarySyncEnabled updates local settings and schedules push`() = runDataTest {
        val source = FakeUserSettingsLocalDataSource().apply {
            settingsState.value = DataFixtures.userSettingsProto
        }
        val scheduler = FakeUserSettingsSyncScheduler()
        val repository = repository(source = source, scheduler = scheduler)

        repository.updateDiarySyncEnabled(DataFixtures.UID, false, 400L).assertOk(Unit)

        val updated = source.updatedSettings.single().toUserSettings()
        assertEquals(false, updated.diarySyncEnabled)
        assertEquals(400L, updated.lastModifiedAt)
        assertEquals(listOf("scheduleSettingsPush"), scheduler.calls)
    }

    @Test
    fun `update failure maps to unexpected and does not schedule push`() = runDataTest {
        val source = FakeUserSettingsLocalDataSource().apply {
            settingsState.value = DataFixtures.userSettingsProto
            failure = IllegalStateException("failure-test")
        }
        val scheduler = FakeUserSettingsSyncScheduler()
        val repository = repository(source = source, scheduler = scheduler)

        repository.updateAppTheme(DataFixtures.UID, AppTheme.LIGHT, 300L)
            .assertErr(DomainError.Unexpected)

        assertEquals(emptyList<String>(), scheduler.calls)
    }

    private fun repository(
        source: FakeUserSettingsLocalDataSource = FakeUserSettingsLocalDataSource(),
        scheduler: FakeUserSettingsSyncScheduler = FakeUserSettingsSyncScheduler(),
    ) = UserSettingsRepositoryImpl(
        logger = FakeAppLogger(),
        localDataSource = source.mock,
        settingsSyncScheduler = scheduler,
    )
}
