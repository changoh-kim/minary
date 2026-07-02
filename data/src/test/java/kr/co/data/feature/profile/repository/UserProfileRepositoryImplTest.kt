package kr.co.data.feature.profile.repository

import kotlinx.coroutines.flow.first
import kr.co.core.common.error.DomainError
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.assertErr
import kr.co.data.testing.assertOk
import kr.co.data.testing.fake.FakeAppLogger
import kr.co.data.testing.fake.FakeImageProcessor
import kr.co.data.testing.fake.FakeUserProfileLocalDataSource
import kr.co.data.testing.fake.FakeUserProfileSyncScheduler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserProfileRepositoryImplTest : BaseDataUnitTest() {
    @Test
    fun `getUserProfileStream maps local profile to AppResult Ok`() = runDataTest {
        val source = FakeUserProfileLocalDataSource().apply {
            profileState.value = DataFixtures.userProfileProto
        }
        val repository = repository(source = source)

        repository.getUserProfileStream().first().assertOk(DataFixtures.userProfile)
    }

    @Test
    fun `updateUserProfile updates local profile and schedules profile push`() = runDataTest {
        val source = FakeUserProfileLocalDataSource()
        val scheduler = FakeUserProfileSyncScheduler()
        val repository = repository(source = source, scheduler = scheduler)

        repository.updateUserProfile(DataFixtures.userProfile).assertOk(Unit)

        assertEquals(DataFixtures.userProfileProto, source.updatedProfiles.single())
        assertEquals(listOf("scheduleProfilePush"), scheduler.calls)
    }

    @Test
    fun `updateUserProfile failure maps to unexpected and does not schedule push`() = runDataTest {
        val source = FakeUserProfileLocalDataSource().apply {
            failure = IllegalStateException("failure-test")
        }
        val scheduler = FakeUserProfileSyncScheduler()
        val repository = repository(source = source, scheduler = scheduler)

        repository.updateUserProfile(DataFixtures.userProfile).assertErr(DomainError.Unexpected)

        assertEquals(emptyList<String>(), scheduler.calls)
    }

    private fun repository(
        source: FakeUserProfileLocalDataSource = FakeUserProfileLocalDataSource(),
        scheduler: FakeUserProfileSyncScheduler = FakeUserProfileSyncScheduler(),
        imageProcessor: FakeImageProcessor = FakeImageProcessor(),
    ) = UserProfileRepositoryImpl(
        logger = FakeAppLogger(),
        localDataSource = source.mock,
        profileScheduler = scheduler,
        imageProcessor = imageProcessor,
    )
}
