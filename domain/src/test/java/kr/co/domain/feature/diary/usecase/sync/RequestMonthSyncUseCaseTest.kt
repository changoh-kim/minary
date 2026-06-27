package kr.co.domain.feature.diary.usecase.sync

import com.github.michaelbull.result.Err
import kr.co.core.common.error.DomainError
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeDiaryRepository
import kr.co.domain.testing.fake.FakeSessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

class RequestMonthSyncUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `requests month sync with current user uid`() {
        runDomainTest {
            val diaryRepository = FakeDiaryRepository()
            val sessionRepository = FakeSessionRepository()
            val useCase = RequestMonthSyncUseCase(sessionRepository, diaryRepository)
            val yearMonth = YearMonth.of(2026, 2)

            val result = useCase(yearMonth)

            result.assertOk(Unit)
            assertEquals(listOf(DomainFixtures.UID to yearMonth), diaryRepository.requestedMonthSyncs)
        }
    }

    @Test
    fun `does not request month sync when current user lookup fails`() {
        runDomainTest {
            val error = DomainError.Auth.UserNotFound
            val diaryRepository = FakeDiaryRepository()
            val sessionRepository = FakeSessionRepository(currentUserResult = Err(error))
            val useCase = RequestMonthSyncUseCase(sessionRepository, diaryRepository)

            val result = useCase(YearMonth.of(2026, 2))

            result.assertErr(error)
            assertEquals(emptyList<Pair<String, YearMonth>>(), diaryRepository.requestedMonthSyncs)
        }
    }

    @Test
    fun `returns repository failure when month sync request fails`() {
        runDomainTest {
            val error = DomainError.Store.PermissionDenied
            val yearMonth = YearMonth.of(2026, 2)
            val diaryRepository = FakeDiaryRepository().apply {
                requestMonthSyncResult = Err(error)
            }
            val sessionRepository = FakeSessionRepository()
            val useCase = RequestMonthSyncUseCase(sessionRepository, diaryRepository)

            val result = useCase(yearMonth)

            result.assertErr(error)
            assertEquals(listOf(DomainFixtures.UID to yearMonth), diaryRepository.requestedMonthSyncs)
        }
    }
}
