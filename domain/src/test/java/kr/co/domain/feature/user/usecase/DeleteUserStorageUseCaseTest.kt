package kr.co.domain.feature.user.usecase

import com.github.michaelbull.result.Err
import kr.co.core.common.error.DomainError
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeUserStorageRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeleteUserStorageUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `deletes storage for requested uid`() {
        runDomainTest {
            val repository = FakeUserStorageRepository()
            val useCase = DeleteUserStorageUseCase(repository)

            val result = useCase(DomainFixtures.UID)

            result.assertOk(Unit)
            assertEquals(listOf(DomainFixtures.UID), repository.deletedUids)
        }
    }

    @Test
    fun `returns repository failure`() {
        runDomainTest {
            val error = DomainError.Storage.PermissionDenied
            val repository = FakeUserStorageRepository(deleteUserStorageResult = Err(error))
            val useCase = DeleteUserStorageUseCase(repository)

            val result = useCase(DomainFixtures.UID)

            result.assertErr(error)
            assertEquals(listOf(DomainFixtures.UID), repository.deletedUids)
        }
    }
}
