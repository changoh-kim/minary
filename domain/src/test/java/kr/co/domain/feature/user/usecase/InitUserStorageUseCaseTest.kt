package kr.co.domain.feature.user.usecase

import com.github.michaelbull.result.Err
import kr.co.core.common.error.DomainError
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeSessionRepository
import kr.co.domain.testing.fake.FakeUserStorageRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InitUserStorageUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `stores current uid without deleting storage when last uid is null`() {
        runDomainTest {
            val sessionRepository = FakeSessionRepository(initialLastSignInUid = null)
            val userStorageRepository = FakeUserStorageRepository()
            val useCase = createUseCase(sessionRepository, userStorageRepository)

            val result = useCase(DomainFixtures.UID)

            result.assertOk(Unit)
            assertEquals(emptyList<String>(), userStorageRepository.deletedUids)
            assertEquals(listOf(DomainFixtures.UID), sessionRepository.setLastSignInUidCalls)
            assertEquals(DomainFixtures.UID, sessionRepository.lastSignInUid)
        }
    }

    @Test
    fun `does not delete storage when last uid is the same as current uid`() {
        runDomainTest {
            val sessionRepository = FakeSessionRepository(initialLastSignInUid = DomainFixtures.UID)
            val userStorageRepository = FakeUserStorageRepository()
            val useCase = createUseCase(sessionRepository, userStorageRepository)

            val result = useCase(DomainFixtures.UID)

            result.assertOk(Unit)
            assertEquals(emptyList<String>(), userStorageRepository.deletedUids)
            assertEquals(listOf(DomainFixtures.UID), sessionRepository.setLastSignInUidCalls)
        }
    }

    @Test
    fun `deletes previous user storage and stores current uid when last uid is different`() {
        runDomainTest {
            val sessionRepository = FakeSessionRepository(initialLastSignInUid = DomainFixtures.OTHER_UID)
            val userStorageRepository = FakeUserStorageRepository()
            val useCase = createUseCase(sessionRepository, userStorageRepository)

            val result = useCase(DomainFixtures.UID)

            result.assertOk(Unit)
            assertEquals(listOf(DomainFixtures.OTHER_UID), userStorageRepository.deletedUids)
            assertEquals(listOf(DomainFixtures.UID), sessionRepository.setLastSignInUidCalls)
            assertEquals(DomainFixtures.UID, sessionRepository.lastSignInUid)
        }
    }

    @Test
    fun `returns deletion failure and does not store current uid when previous storage deletion fails`() {
        runDomainTest {
            val error = DomainError.Storage.PermissionDenied
            val sessionRepository = FakeSessionRepository(initialLastSignInUid = DomainFixtures.OTHER_UID)
            val userStorageRepository = FakeUserStorageRepository(deleteUserStorageResult = Err(error))
            val useCase = createUseCase(sessionRepository, userStorageRepository)

            val result = useCase(DomainFixtures.UID)

            result.assertErr(error)
            assertEquals(listOf(DomainFixtures.OTHER_UID), userStorageRepository.deletedUids)
            assertEquals(emptyList<String>(), sessionRepository.setLastSignInUidCalls)
            assertEquals(DomainFixtures.OTHER_UID, sessionRepository.lastSignInUid)
        }
    }

    private fun createUseCase(
        sessionRepository: FakeSessionRepository,
        userStorageRepository: FakeUserStorageRepository,
    ): InitUserStorageUseCase = InitUserStorageUseCase(
        sessionRepository = sessionRepository,
        deleteUserStorage = DeleteUserStorageUseCase(userStorageRepository),
    )
}
