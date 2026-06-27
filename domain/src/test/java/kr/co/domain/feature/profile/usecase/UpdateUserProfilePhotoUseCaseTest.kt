package kr.co.domain.feature.profile.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kr.co.core.common.error.DomainError
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.FakeServerTimeProvider
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeSessionRepository
import kr.co.domain.testing.fake.FakeUserProfileRepository
import kr.co.domain.testing.fake.UserProfilePhotoUpdate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpdateUserProfilePhotoUseCaseTest : DomainCoroutineTest() {

    private val serverTime = FakeServerTimeProvider(currentTime = DomainFixtures.FIXED_TIME)

    @Test
    fun `updates profile photo with current user uid and server time`() {
        runDomainTest {
            val profileRepository = FakeUserProfileRepository().apply {
                updateUserProfilePhotoResult = Ok("updated-photo-url-test")
            }
            val sessionRepository = FakeSessionRepository()
            val useCase = createUseCase(sessionRepository, profileRepository)

            val result = useCase("photo-url-test")

            result.assertOk("updated-photo-url-test")
            assertEquals(
                listOf(
                    UserProfilePhotoUpdate(
                        uid = DomainFixtures.UID,
                        photoUrl = "photo-url-test",
                        lastModifiedAt = DomainFixtures.FIXED_TIME,
                    )
                ),
                profileRepository.profilePhotoUpdates,
            )
        }
    }

    @Test
    fun `does not update profile photo when current user lookup fails`() {
        runDomainTest {
            val error = DomainError.Auth.UserNotFound
            val profileRepository = FakeUserProfileRepository()
            val sessionRepository = FakeSessionRepository(currentUserResult = Err(error))
            val useCase = createUseCase(sessionRepository, profileRepository)

            val result = useCase("photo-url-test")

            result.assertErr(error)
            assertEquals(emptyList<UserProfilePhotoUpdate>(), profileRepository.profilePhotoUpdates)
        }
    }

    @Test
    fun `returns repository failure when profile photo update fails`() {
        runDomainTest {
            val error = DomainError.Storage.PermissionDenied
            val profileRepository = FakeUserProfileRepository().apply {
                updateUserProfilePhotoResult = Err(error)
            }
            val sessionRepository = FakeSessionRepository()
            val useCase = createUseCase(sessionRepository, profileRepository)

            val result = useCase("photo-url-test")

            result.assertErr(error)
            assertEquals(1, profileRepository.profilePhotoUpdates.size)
        }
    }

    private fun createUseCase(
        sessionRepository: FakeSessionRepository,
        profileRepository: FakeUserProfileRepository,
    ): UpdateUserProfilePhotoUseCase = UpdateUserProfilePhotoUseCase(
        sessionRepository = sessionRepository,
        userProfileRepository = profileRepository,
        serverTimeProvider = serverTime,
    )
}
