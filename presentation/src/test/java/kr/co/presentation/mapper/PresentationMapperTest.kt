package kr.co.presentation.mapper

import kr.co.presentation.app.mapper.UserSessionUiModelMapper.toUserSession
import kr.co.presentation.app.mapper.UserSessionUiModelMapper.toUserSessionUiModel
import kr.co.presentation.feature.account.mapper.AccountUiModelMapper.toAccount
import kr.co.presentation.feature.account.mapper.AccountUiModelMapper.toAccountUiModel
import kr.co.presentation.feature.calendar.mapper.CalendarDiaryUiMapper.toCalendarDiaryUiModel
import kr.co.presentation.feature.dashboard.mapper.DashboardUiModelMapper.toDashboardUiModel
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiary
import kr.co.presentation.feature.diary.mapper.DiaryUiModelMapper.toDiaryUiModel
import kr.co.presentation.feature.search.mapper.SearchDiaryUiModelMapper.toSearchDiaryUiModel
import kr.co.presentation.feature.setting.mapper.UserProfileUiModelMapper.toUserProfile
import kr.co.presentation.feature.setting.mapper.UserProfileUiModelMapper.toUserProfileUiModel
import kr.co.presentation.feature.setting.mapper.UserSettingsUiModelMapper.toUserSettings
import kr.co.presentation.feature.setting.mapper.UserSettingsUiModelMapper.toUserSettingsUiModel
import kr.co.presentation.testing.PresentationFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PresentationMapperTest {

    @Test
    fun `account mapper preserves uid and email in both directions`() {
        assertEquals(PresentationFixtures.accountUiModel(), PresentationFixtures.account().toAccountUiModel())
        assertEquals(PresentationFixtures.account(), PresentationFixtures.accountUiModel().toAccount())
    }

    @Test
    fun `user session mapper preserves uid and email in both directions`() {
        assertEquals(PresentationFixtures.userSessionUiModel(), PresentationFixtures.userSession().toUserSessionUiModel())
        assertEquals(PresentationFixtures.userSession(), PresentationFixtures.userSessionUiModel().toUserSession())
    }

    @Test
    fun `diary mapper preserves editable diary fields in both directions`() {
        val diary = PresentationFixtures.diary()
        val uiModel = PresentationFixtures.diaryUiModel()

        assertEquals(uiModel.copy(timestamp = uiModel.updatedAt), diary.toDiaryUiModel().copy(timestamp = uiModel.updatedAt))
        assertEquals(diary, uiModel.toDiary())
    }

    @Test
    fun `calendar and search diary mappers preserve preview fields`() {
        val diary = PresentationFixtures.diary()

        assertEquals(PresentationFixtures.DATE, diary.toCalendarDiaryUiModel().date)
        assertEquals(PresentationFixtures.TITLE, diary.toCalendarDiaryUiModel().title)
        assertEquals(PresentationFixtures.IMAGE_URL, diary.toSearchDiaryUiModel().imageUrls.single())
    }

    @Test
    fun `dashboard mapper preserves statistics and nullable recent diary entries`() {
        assertEquals(PresentationFixtures.dashboardUiModel(), PresentationFixtures.dashboard().toDashboardUiModel())
    }

    @Test
    fun `user profile mapper preserves profile fields in both directions`() {
        assertEquals(PresentationFixtures.userProfileUiModel(), PresentationFixtures.userProfile().toUserProfileUiModel())
        assertEquals(PresentationFixtures.userProfile(), PresentationFixtures.userProfileUiModel().toUserProfile())
    }

    @Test
    fun `user settings mapper preserves theme sync and timestamp in both directions`() {
        assertEquals(PresentationFixtures.userSettingsUiModel(), PresentationFixtures.userSettings().toUserSettingsUiModel())
        assertEquals(PresentationFixtures.userSettings(), PresentationFixtures.userSettingsUiModel().toUserSettings())
    }
}
