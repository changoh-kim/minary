package kr.co.domain.feature.search.usecase

import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.fake.FakeSearchRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RemoveRecentSearchUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `removes query from repository`() {
        runDomainTest {
            val repository = FakeSearchRepository(initialSearches = listOf("query-test", "other-query-test"))
            val useCase = RemoveRecentSearchUseCase(repository)

            useCase("query-test")

            assertEquals(listOf("query-test"), repository.removedSearches)
        }
    }

    @Test
    fun `propagates repository exception`() {
        val exception = IllegalStateException("failure-test")
        val repository = FakeSearchRepository().apply {
            removeFailure = exception
        }
        val useCase = RemoveRecentSearchUseCase(repository)

        val actual = assertThrows(IllegalStateException::class.java) {
            runDomainTest {
                useCase("query-test")
            }
        }

        assertEquals(exception, actual)
    }
}
