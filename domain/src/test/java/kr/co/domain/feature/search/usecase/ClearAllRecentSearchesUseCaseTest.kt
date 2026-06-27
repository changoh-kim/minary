package kr.co.domain.feature.search.usecase

import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.fake.FakeSearchRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ClearAllRecentSearchesUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `clears all recent searches`() {
        runDomainTest {
            val repository = FakeSearchRepository(initialSearches = listOf("query-test"))
            val useCase = ClearAllRecentSearchesUseCase(repository)

            useCase()

            assertEquals(1, repository.clearAllCallCount)
        }
    }

    @Test
    fun `propagates repository exception`() {
        val exception = IllegalStateException("failure-test")
        val repository = FakeSearchRepository().apply {
            clearFailure = exception
        }
        val useCase = ClearAllRecentSearchesUseCase(repository)

        val actual = assertThrows(IllegalStateException::class.java) {
            runDomainTest {
                useCase()
            }
        }

        assertEquals(exception, actual)
    }
}
