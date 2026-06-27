package kr.co.domain.feature.setting.usecase

import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kr.co.core.common.model.AppTheme
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.setting.model.UserSettings
import kr.co.domain.feature.setting.repository.UserSettingsRepository
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetUserSettingsStreamUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `removes consecutive duplicate settings emissions`() {
        runDomainTest {
            val settings = DomainFixtures.userSettings()
            val changed = settings.copy(diarySyncEnabled = true)
            val repository = SettingsStreamRepository(
                settingsStream = flowOf(
                    Ok(settings),
                    Ok(settings),
                    Ok(changed),
                )
            )
            val useCase = GetUserSettingsStreamUseCase(repository)

            val result = useCase().toList()

            assertEquals(listOf(Ok(settings), Ok(changed)), result)
        }
    }

    private class SettingsStreamRepository(
        private val settingsStream: Flow<AppResult<UserSettings>>,
    ) : UserSettingsRepository {
        override suspend fun getUserSettingsStream(): Flow<AppResult<UserSettings>> = settingsStream

        override fun getAppThemeStream(): Flow<AppTheme> {
            error("unused")
        }

        override suspend fun updateAppTheme(
            uid: String,
            appTheme: AppTheme,
            lastModifiedAt: Long,
        ): AppResult<Unit> {
            error("unused")
        }

        override fun getDiarySyncEnabledStream(): Flow<Boolean> {
            error("unused")
        }

        override suspend fun updateDiarySyncEnabled(
            uid: String,
            enabled: Boolean,
            lastModifiedAt: Long,
        ): AppResult<Unit> {
            error("unused")
        }
    }
}
