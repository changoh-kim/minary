package kr.co.domain.feature.diary.usecase.sync

import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeDiarySyncStateRepository
import org.junit.jupiter.api.Test

class CheckDiarySyncStateUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `returns false when pending count is zero`() {
        runDomainTest {
            val syncStateRepository = FakeDiarySyncStateRepository(initialPendingCount = 0)
            val useCase = CheckDiarySyncStateUseCase(syncStateRepository)

            val result = useCase()

            result.assertOk(false)
        }
    }

    @Test
    fun `returns true when pending count is greater than zero`() {
        runDomainTest {
            val syncStateRepository = FakeDiarySyncStateRepository(initialPendingCount = 1)
            val useCase = CheckDiarySyncStateUseCase(syncStateRepository)

            val result = useCase()

            result.assertOk(true)
        }
    }
}
