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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetAppThemeStreamUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `removes consecutive duplicate app theme emissions`() {
        runDomainTest {
            val repository = AppThemeStreamRepository(
                flowOf(
                    AppTheme.DARK,
                    AppTheme.DARK,
                    AppTheme.LIGHT,
                )
            )
            val useCase = GetAppThemeStreamUseCase(repository)

            val result = useCase().toList()

            assertEquals(listOf(AppTheme.DARK, AppTheme.LIGHT), result)
        }
    }

    private class AppThemeStreamRepository(
        private val appThemeStream: Flow<AppTheme>,
    ) : UserSettingsRepository {
        override suspend fun getUserSettingsStream(): Flow<AppResult<UserSettings>> {
            error("unused")
        }

        override fun getAppThemeStream(): Flow<AppTheme> = appThemeStream

        override suspend fun updateAppTheme(
            uid: String,
            appTheme: AppTheme,
            lastModifiedAt: Long,
        ): AppResult<Unit> = Ok(Unit)

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
