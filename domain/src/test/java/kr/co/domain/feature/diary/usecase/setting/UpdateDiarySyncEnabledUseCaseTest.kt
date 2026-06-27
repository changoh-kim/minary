package kr.co.domain.feature.diary.usecase.setting

import com.github.michaelbull.result.Err
import kr.co.core.common.error.DomainError
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.FakeServerTimeProvider
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.DiarySyncEnabledUpdate
import kr.co.domain.testing.fake.FakeSessionRepository
import kr.co.domain.testing.fake.FakeUserSettingsRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpdateDiarySyncEnabledUseCaseTest : DomainCoroutineTest() {

    private val serverTime = FakeServerTimeProvider(currentTime = DomainFixtures.FIXED_TIME)

    @Test
    fun `updates diary sync enabled with current user uid and server time`() {
        runDomainTest {
            val settingsRepository = FakeUserSettingsRepository()
            val sessionRepository = FakeSessionRepository()
            val useCase = createUseCase(sessionRepository, settingsRepository)

            val result = useCase(enabled = true)

            result.assertOk(Unit)
            assertEquals(
                listOf(
                    DiarySyncEnabledUpdate(
                        uid = DomainFixtures.UID,
                        enabled = true,
                        lastModifiedAt = DomainFixtures.FIXED_TIME,
                    )
                ),
                settingsRepository.diarySyncEnabledUpdates,
            )
        }
    }

    @Test
    fun `does not update diary sync setting when current user lookup fails`() {
        runDomainTest {
            val error = DomainError.Auth.UserNotFound
            val settingsRepository = FakeUserSettingsRepository()
            val sessionRepository = FakeSessionRepository(currentUserResult = Err(error))
            val useCase = createUseCase(sessionRepository, settingsRepository)

            val result = useCase(enabled = false)

            result.assertErr(error)
            assertEquals(emptyList<DiarySyncEnabledUpdate>(), settingsRepository.diarySyncEnabledUpdates)
        }
    }

    @Test
    fun `returns repository failure when diary sync setting update fails`() {
        runDomainTest {
            val error = DomainError.Store.PermissionDenied
            val settingsRepository = FakeUserSettingsRepository().apply {
                updateDiarySyncEnabledResult = Err(error)
            }
            val sessionRepository = FakeSessionRepository()
            val useCase = createUseCase(sessionRepository, settingsRepository)

            val result = useCase(enabled = true)

            result.assertErr(error)
            assertEquals(1, settingsRepository.diarySyncEnabledUpdates.size)
        }
    }

    private fun createUseCase(
        sessionRepository: FakeSessionRepository,
        settingsRepository: FakeUserSettingsRepository,
    ): UpdateDiarySyncEnabledUseCase = UpdateDiarySyncEnabledUseCase(
        sessionRepository = sessionRepository,
        serverTimeProvider = serverTime,
        settingsRepository = settingsRepository,
    )
}
