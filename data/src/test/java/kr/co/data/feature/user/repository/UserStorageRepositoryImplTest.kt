package kr.co.data.feature.user.repository

import kr.co.core.common.error.DomainError
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.assertErr
import kr.co.data.testing.assertOk
import kr.co.data.testing.fake.FakeUserStorageLocalDataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserStorageRepositoryImplTest : BaseDataUnitTest() {
    @Test
    fun `deleteUserStorage delegates uid to local source`() = runDataTest {
        val source = FakeUserStorageLocalDataSource()
        val repository = UserStorageRepositoryImpl(source.mock)

        repository.deleteUserStorage(DataFixtures.UID).assertOk(Unit)

        assertEquals(listOf(DataFixtures.UID), source.deletedUids)
    }

    @Test
    fun `deleteUserStorage maps local exception to unexpected`() = runDataTest {
        val source = FakeUserStorageLocalDataSource().apply {
            failure = IllegalStateException("failure-test")
        }
        val repository = UserStorageRepositoryImpl(source.mock)

        repository.deleteUserStorage(DataFixtures.UID).assertErr(DomainError.Unexpected)
    }
}
