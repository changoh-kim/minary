package kr.co.domain.feature.diary.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kr.co.core.common.error.DomainError
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.assertErr
import kr.co.domain.testing.assertOk
import kr.co.domain.testing.fake.FakeDiaryRepository
import kr.co.domain.testing.fake.PagedDiariesRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GetPagedDiariesUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `passes paging filters to repository and returns diaries`() {
        runDomainTest {
            val diaries = listOf(DomainFixtures.diary())
            val repository = FakeDiaryRepository().apply {
                getPagedDiariesResult = Ok(diaries)
            }
            val useCase = GetPagedDiariesUseCase(repository)
            val startDate = LocalDate.of(2026, 1, 1)
            val endDate = LocalDate.of(2026, 1, 31)

            val result = useCase(
                query = "query-test",
                startDate = startDate,
                endDate = endDate,
                limit = 20,
                offset = 40,
            )

            result.assertOk(diaries)
            assertEquals(
                listOf(
                    PagedDiariesRequest(
                        query = "query-test",
                        startDate = startDate,
                        endDate = endDate,
                        limit = 20,
                        offset = 40,
                    )
                ),
                repository.pagedDiaryRequests,
            )
        }
    }

    @Test
    fun `returns repository failure`() {
        runDomainTest {
            val error = DomainError.Store.NotFound
            val repository = FakeDiaryRepository().apply {
                getPagedDiariesResult = Err(error)
            }
            val useCase = GetPagedDiariesUseCase(repository)

            val result = useCase(limit = 10, offset = 0)

            result.assertErr(error)
        }
    }
}
