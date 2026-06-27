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

class CreateDiaryUseCaseTest : DomainCoroutineTest() {

    private val serverTime = FakeServerTimeProvider(currentTime = DomainFixtures.FIXED_TIME)

    @Test
    fun `creates diary with server timestamps and pending create status`() {
        runDomainTest {
            val diaryRepository = FakeDiaryRepository()
            val useCase = CreateDiaryUseCase(diaryRepository, serverTime)
            val input = DomainFixtures.diary(
                createdAt = 1L,
                updatedAt = 2L,
                syncStatus = DiarySyncStatus.SYNCED,
            )

            val result = useCase(input)

            result.assertOk(Unit)
            assertEquals(1, diaryRepository.createdDiaries.size)
            assertEquals(
                input.copy(
                    createdAt = DomainFixtures.FIXED_TIME,
                    updatedAt = DomainFixtures.FIXED_TIME,
                    syncStatus = DiarySyncStatus.PENDING_CREATE,
                ),
                diaryRepository.createdDiaries.single(),
            )
        }
    }

    @Test
    fun `returns repository failure when create diary fails`() {
        runDomainTest {
            val error = DomainError.Store.PermissionDenied
            val diaryRepository = FakeDiaryRepository().apply {
                createDiaryResult = Err(error)
            }
            val useCase = CreateDiaryUseCase(diaryRepository, serverTime)

            val result = useCase(DomainFixtures.diary())

            result.assertErr(error)
            assertEquals(1, diaryRepository.createdDiaries.size)
        }
    }
}
