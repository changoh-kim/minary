package kr.co.data.testing.fake

import com.github.michaelbull.result.get
import kotlinx.coroutines.flow.first
import kr.co.core.common.model.AppTheme
import kr.co.core.datastore.proto.ThemeProto
import kr.co.data.feature.search.repository.SearchRepositoryImpl
import kr.co.data.feature.session.repository.SessionRepositoryImpl
import kr.co.data.feature.setting.repository.UserSettingsRepositoryImpl
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataFakeInfrastructureTest : BaseDataUnitTest() {
    @Test
    fun `fake search source records repository commands`() = runDataTest {
        val fakeSource = FakeSearchLocalDataSource()
        val repository = SearchRepositoryImpl(fakeSource.mock)

        repository.addRecentSearch("query-test", 100L)
        repository.removeRecentSearch("query-test")
        repository.clearAll()

        assertEquals("query-test", fakeSource.addedSearches.single().query)
        assertEquals(100L, fakeSource.addedSearches.single().timestamp)
        assertEquals(listOf("query-test"), fakeSource.removedQueries)
        assertEquals(1, fakeSource.clearAllCallCount)
    }

    @Test
    fun `fake session sources drive session repository`() = runDataTest {
        val fakeLocal = FakeSessionLocalDataSource()
        val fakeRemote = FakeSessionRemoteDataSource().apply {
            currentUser = DataFixtures.userSessionModel
            sessionState.value = DataFixtures.userSessionModel
        }
        val repository = SessionRepositoryImpl(
            logger = FakeAppLogger(),
            localDataSource = fakeLocal.mock,
            remoteDataSource = fakeRemote.mock,
        )

        repository.setLastSignInUid(DataFixtures.UID)

        assertEquals(DataFixtures.UID, repository.getLastSignInUid())
        assertEquals(DataFixtures.userSession, repository.getCurrentUser().get())
        assertEquals(DataFixtures.userSession, repository.getSessionStateStream().first())
    }

    @Test
    fun `fake settings source and scheduler drive settings repository`() = runDataTest {
        val fakeSource = FakeUserSettingsLocalDataSource().apply {
            settingsState.value = DataFixtures.userSettingsProto
        }
        val scheduler = FakeUserSettingsSyncScheduler()
        val repository = UserSettingsRepositoryImpl(
            logger = FakeAppLogger(),
            localDataSource = fakeSource.mock,
            settingsSyncScheduler = scheduler,
        )

        val result = repository.updateAppTheme(
            uid = DataFixtures.UID,
            appTheme = AppTheme.LIGHT,
            lastModifiedAt = 300L,
        )

        assertEquals(Unit, result.get())
        assertEquals(ThemeProto.LIGHT, fakeSource.updatedSettings.single().theme)
        assertEquals(300L, fakeSource.updatedSettings.single().lastModifiedAt)
        assertEquals(listOf("scheduleSettingsPush"), scheduler.calls)
    }
}
