package kr.co.domain.feature.setting.usecase

import com.github.michaelbull.result.Err
import kr.co.core.common.error.DomainError
import kr.co.core.common.model.AppTheme
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.FakeServerTimeProvider
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.AppThemeUpdate
import kr.co.domain.testing.fake.FakeSessionRepository
import kr.co.domain.testing.fake.FakeUserSettingsRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpdateAppThemeUseCaseTest : DomainCoroutineTest() {

    private val serverTime = FakeServerTimeProvider(currentTime = DomainFixtures.FIXED_TIME)

    @Test
    fun `updates app theme with current user uid and server time`() {
        runDomainTest {
            val settingsRepository = FakeUserSettingsRepository()
            val sessionRepository = FakeSessionRepository()
            val useCase = createUseCase(sessionRepository, settingsRepository)

            val result = useCase(AppTheme.DARK)

            result.assertOk(Unit)
            assertEquals(
                listOf(
                    AppThemeUpdate(
                        uid = DomainFixtures.UID,
                        appTheme = AppTheme.DARK,
                        lastModifiedAt = DomainFixtures.FIXED_TIME,
                    )
                ),
                settingsRepository.appThemeUpdates,
            )
        }
    }

    @Test
    fun `does not update app theme when current user lookup fails`() {
        runDomainTest {
            val error = DomainError.Auth.UserNotFound
            val settingsRepository = FakeUserSettingsRepository()
            val sessionRepository = FakeSessionRepository(currentUserResult = Err(error))
            val useCase = createUseCase(sessionRepository, settingsRepository)

            val result = useCase(AppTheme.LIGHT)

            result.assertErr(error)
            assertEquals(emptyList<AppThemeUpdate>(), settingsRepository.appThemeUpdates)
        }
    }

    @Test
    fun `returns repository failure when app theme update fails`() {
        runDomainTest {
            val error = DomainError.Store.PermissionDenied
            val settingsRepository = FakeUserSettingsRepository().apply {
                updateAppThemeResult = Err(error)
            }
            val sessionRepository = FakeSessionRepository()
            val useCase = createUseCase(sessionRepository, settingsRepository)

            val result = useCase(AppTheme.LIGHT)

            result.assertErr(error)
            assertEquals(1, settingsRepository.appThemeUpdates.size)
        }
    }

    private fun createUseCase(
        sessionRepository: FakeSessionRepository,
        settingsRepository: FakeUserSettingsRepository,
    ): UpdateAppThemeUseCase = UpdateAppThemeUseCase(
        sessionRepository = sessionRepository,
        serverTimeProvider = serverTime,
        settingsRepository = settingsRepository,
    )
}
