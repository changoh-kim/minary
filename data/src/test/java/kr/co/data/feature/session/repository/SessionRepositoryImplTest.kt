package kr.co.data.feature.session.repository

import kotlinx.coroutines.flow.first
import kr.co.core.common.error.DomainError
import kr.co.data.feature.account.exception.AccountException
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.assertErr
import kr.co.data.testing.assertOk
import kr.co.data.testing.fake.FakeAppLogger
import kr.co.data.testing.fake.FakeSessionLocalDataSource
import kr.co.data.testing.fake.FakeSessionRemoteDataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SessionRepositoryImplTest : BaseDataUnitTest() {
    @Test
    fun `last sign in uid is delegated to local source`() = runDataTest {
        val local = FakeSessionLocalDataSource()
        val repository = repository(local = local)

        repository.setLastSignInUid(DataFixtures.UID)

        assertEquals(DataFixtures.UID, repository.getLastSignInUid())
        assertEquals(listOf(DataFixtures.UID), local.setLastSignInUidCalls)
    }

    @Test
    fun `getCurrentUser maps remote model to domain user session`() = runDataTest {
        val remote = FakeSessionRemoteDataSource().apply {
            currentUser = DataFixtures.userSessionModel
        }
        val repository = repository(remote = remote)

        repository.getCurrentUser().assertOk(DataFixtures.userSession)
    }

    @Test
    fun `getCurrentUser maps remote exception to domain error and logs`() = runDataTest {
        val logger = FakeAppLogger()
        val remote = FakeSessionRemoteDataSource().apply {
            currentUserFailure = AccountException.UserNotFoundException()
        }
        val repository = repository(logger = logger, remote = remote)

        repository.getCurrentUser().assertErr(DomainError.Auth.UserNotFound)

        assertEquals("Failed to get current user", logger.errorThrowables.single().second)
    }

    @Test
    fun `reload maps remote model to domain user session`() = runDataTest {
        val remote = FakeSessionRemoteDataSource().apply {
            reloadUser = DataFixtures.userSessionModel
        }
        val repository = repository(remote = remote)

        repository.reload().assertOk(DataFixtures.userSession)
    }

    @Test
    fun `session state stream maps nullable remote model`() = runDataTest {
        val remote = FakeSessionRemoteDataSource().apply {
            sessionState.value = DataFixtures.userSessionModel
        }
        val repository = repository(remote = remote)

        assertEquals(DataFixtures.userSession, repository.getSessionStateStream().first())

        remote.sessionState.value = null

        assertEquals(null, repository.getSessionStateStream().first())
    }

    private fun repository(
        logger: FakeAppLogger = FakeAppLogger(),
        local: FakeSessionLocalDataSource = FakeSessionLocalDataSource(),
        remote: FakeSessionRemoteDataSource = FakeSessionRemoteDataSource(),
    ) = SessionRepositoryImpl(
        logger = logger,
        localDataSource = local.mock,
        remoteDataSource = remote.mock,
    )
}
