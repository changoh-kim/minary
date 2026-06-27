package kr.co.domain.feature.diary.usecase

import com.github.michaelbull.result.Err
import kr.co.core.common.error.DomainError
import kr.co.core.common.state.DiarySyncStatus
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.FakeServerTimeProvider
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeDiaryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DeleteDiaryUseCaseTest : DomainCoroutineTest() {

    private val serverTime = FakeServerTimeProvider(currentTime = DomainFixtures.FIXED_TIME)

    @Test
    fun `deletes diary with server updatedAt and pending delete status`() {
        runDomainTest {
            val diaryRepository = FakeDiaryRepository()
            val useCase = DeleteDiaryUseCase(diaryRepository, serverTime)
            val input = DomainFixtures.diary(
                createdAt = 1L,
                updatedAt = 2L,
                syncStatus = DiarySyncStatus.SYNCED,
            )
            val expected = input.copy(
                updatedAt = DomainFixtures.FIXED_TIME,
                syncStatus = DiarySyncStatus.PENDING_DELETE,
            )

            val result = useCase(input)

            result.assertOk(Unit)
            assertEquals(listOf(expected), diaryRepository.deletedDiaries)
        }
    }

    @Test
    fun `returns repository failure when delete diary fails`() {
        runDomainTest {
            val error = DomainError.Store.PermissionDenied
            val diaryRepository = FakeDiaryRepository().apply {
                deleteDiaryResult = Err(error)
            }
            val useCase = DeleteDiaryUseCase(diaryRepository, serverTime)

            val result = useCase(DomainFixtures.diary())

            result.assertErr(error)
            assertEquals(1, diaryRepository.deletedDiaries.size)
        }
    }
}
