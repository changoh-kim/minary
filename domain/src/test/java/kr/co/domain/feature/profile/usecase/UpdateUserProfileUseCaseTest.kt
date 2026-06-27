package kr.co.domain.feature.profile.usecase

import com.github.michaelbull.result.Err
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.FakeServerTimeProvider
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeSessionRepository
import kr.co.domain.testing.fake.FakeUserProfileRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpdateUserProfileUseCaseTest : DomainCoroutineTest() {

    private val serverTime = FakeServerTimeProvider(currentTime = DomainFixtures.FIXED_TIME)

    @Test
    fun `updates profile with current user uid and server time`() {
        runDomainTest {
            val profileRepository = FakeUserProfileRepository()
            val sessionRepository = FakeSessionRepository()
            val useCase = createUseCase(sessionRepository, profileRepository)
            val input = DomainFixtures.userProfile(
                uid = DomainFixtures.OTHER_UID,
                lastModifiedAt = 1L,
            )
            val expected = input.copy(
                uid = DomainFixtures.UID,
                lastModifiedAt = DomainFixtures.FIXED_TIME,
            )

            val result = useCase(input)

            result.assertOk(Unit)
            assertEquals(listOf(expected), profileRepository.updatedProfiles)
        }
    }

    @Test
    fun `does not update profile when current user lookup fails`() {
        runDomainTest {
            val error = DomainError.Auth.UserNotFound
            val profileRepository = FakeUserProfileRepository()
            val sessionRepository = FakeSessionRepository(currentUserResult = Err(error))
            val useCase = createUseCase(sessionRepository, profileRepository)

            val result = useCase(DomainFixtures.userProfile())

            result.assertErr(error)
            assertEquals(emptyList<UserProfile>(), profileRepository.updatedProfiles)
        }
    }

    @Test
    fun `returns repository failure when profile update fails`() {
        runDomainTest {
            val error = DomainError.Store.PermissionDenied
            val profileRepository = FakeUserProfileRepository().apply {
                updateUserProfileResult = Err(error)
            }
            val sessionRepository = FakeSessionRepository()
            val useCase = createUseCase(sessionRepository, profileRepository)

            val result = useCase(DomainFixtures.userProfile())

            result.assertErr(error)
            assertEquals(1, profileRepository.updatedProfiles.size)
        }
    }

    private fun createUseCase(
        sessionRepository: FakeSessionRepository,
        profileRepository: FakeUserProfileRepository,
    ): UpdateUserProfileUseCase = UpdateUserProfileUseCase(
        sessionRepository = sessionRepository,
        userProfileRepository = profileRepository,
        serverTimeProvider = serverTime,
    )
}
