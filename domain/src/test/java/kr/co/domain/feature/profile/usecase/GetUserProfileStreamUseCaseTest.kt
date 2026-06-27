package kr.co.domain.feature.profile.usecase

import com.github.michaelbull.result.Ok
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.profile.model.UserProfile
import kr.co.domain.feature.profile.repository.UserProfileRepository
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetUserProfileStreamUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `removes consecutive duplicate profile emissions`() {
        runDomainTest {
            val profile = DomainFixtures.userProfile()
            val changed = profile.copy(nickname = "nickname-updated-test")
            val repository = ProfileStreamRepository(
                flowOf(
                    Ok(profile),
                    Ok(profile),
                    Ok(changed),
                )
            )
            val useCase = GetUserProfileStreamUseCase(repository)

            val result = useCase().toList()

            assertEquals(listOf(Ok(profile), Ok(changed)), result)
        }
    }

    private class ProfileStreamRepository(
        private val stream: Flow<AppResult<UserProfile>>,
    ) : UserProfileRepository {
        override suspend fun getUserProfileStream(): Flow<AppResult<UserProfile>> = stream

        override suspend fun updateUserProfile(profile: UserProfile): AppResult<Unit> {
            error("unused")
        }

        override suspend fun updateUserProfilePhoto(
            uid: String,
            photoUrl: String,
            lastModifiedAt: Long,
        ): AppResult<String> {
            error("unused")
        }
    }
}
