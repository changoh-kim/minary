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

class UpdateDiaryUseCaseTest : DomainCoroutineTest() {

    private val serverTime = FakeServerTimeProvider(currentTime = DomainFixtures.FIXED_TIME)

    @Test
    fun `updates diary with server updatedAt and pending update status`() {
        runDomainTest {
            val diaryRepository = FakeDiaryRepository()
            val useCase = UpdateDiaryUseCase(diaryRepository, serverTime)
            val input = DomainFixtures.diary(
                createdAt = 1L,
                updatedAt = 2L,
                syncStatus = DiarySyncStatus.SYNCED,
            )
            val expected = input.copy(
                updatedAt = DomainFixtures.FIXED_TIME,
                syncStatus = DiarySyncStatus.PENDING_UPDATE,
            )

            val result = useCase(input)

            result.assertOk(expected)
            assertEquals(listOf(expected), diaryRepository.updatedDiaries)
        }
    }

    @Test
    fun `returns repository failure when update diary fails`() {
        runDomainTest {
            val error = DomainError.Diary.NotFound
            val diaryRepository = FakeDiaryRepository().apply {
                updateDiaryResult = Err(error)
            }
            val useCase = UpdateDiaryUseCase(diaryRepository, serverTime)

            val result = useCase(DomainFixtures.diary())

            result.assertErr(error)
            assertEquals(1, diaryRepository.updatedDiaries.size)
        }
    }
}
