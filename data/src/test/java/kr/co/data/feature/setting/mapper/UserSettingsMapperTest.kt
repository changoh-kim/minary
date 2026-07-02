package kr.co.data.feature.setting.mapper

import kr.co.core.common.model.AppTheme
import kr.co.core.datastore.proto.ThemeProto
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toAppTheme
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toThemeProto
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toUserSettings
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toUserSettingsDto
import kr.co.data.feature.setting.mapper.UserSettingsMapper.toUserSettingsProto
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserSettingsMapperTest : BaseDataUnitTest() {
    @Test
    fun `maps settings proto to domain settings`() {
        assertEquals(DataFixtures.userSettings, DataFixtures.userSettingsProto.toUserSettings())
    }

    @Test
    fun `maps domain settings to settings proto`() {
        assertEquals(DataFixtures.userSettingsProto, DataFixtures.userSettings.toUserSettingsProto())
    }

    @Test
    fun `maps settings proto to dto`() {
        assertEquals(DataFixtures.userSettingsDto, DataFixtures.userSettingsProto.toUserSettingsDto())
    }

    @Test
    fun `maps settings dto to proto`() {
        assertEquals(DataFixtures.userSettingsProto, DataFixtures.userSettingsDto.toUserSettingsProto())
    }

    @Test
    fun `maps theme proto fallback to system`() {
        assertEquals(AppTheme.SYSTEM, ThemeProto.UNRECOGNIZED.toAppTheme())
    }

    @Test
    fun `maps app theme fallback to system proto`() {
        assertEquals(ThemeProto.SYSTEM, AppTheme.SYSTEM.toThemeProto())
    }

    @Test
    fun `maps unknown settings dto theme to system`() {
        val dto = DataFixtures.userSettingsDto.copy(theme = "unknown-value-test")

        assertEquals(ThemeProto.SYSTEM, dto.toUserSettingsProto().theme)
    }
}
