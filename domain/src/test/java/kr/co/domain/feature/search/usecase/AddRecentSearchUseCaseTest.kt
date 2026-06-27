package kr.co.domain.feature.search.usecase

import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.DomainFixtures
import kr.co.domain.testing.FakeServerTimeProvider
import kr.co.domain.testing.fake.FakeSearchRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AddRecentSearchUseCaseTest : DomainCoroutineTest() {

    private val serverTime = FakeServerTimeProvider(currentTime = DomainFixtures.FIXED_TIME)

    @Test
    fun `does not add blank query`() {
        runDomainTest {
            val searchRepository = FakeSearchRepository()
            val useCase = AddRecentSearchUseCase(searchRepository, serverTime)

            useCase("   ")

            assertEquals(emptyList<Pair<String, Long>>(), searchRepository.addedSearches)
        }
    }

    @Test
    fun `adds trimmed query with server time`() {
        runDomainTest {
            val searchRepository = FakeSearchRepository()
            val useCase = AddRecentSearchUseCase(searchRepository, serverTime)

            useCase("  query-test  ")

            assertEquals(
                listOf("query-test" to DomainFixtures.FIXED_TIME),
                searchRepository.addedSearches,
            )
        }
    }

    @Test
    fun `propagates repository exception`() {
        val exception = IllegalStateException("failure-test")
        val searchRepository = FakeSearchRepository().apply {
            addFailure = exception
        }
        val useCase = AddRecentSearchUseCase(searchRepository, serverTime)

        val actual = assertThrows(IllegalStateException::class.java) {
            runDomainTest {
                useCase("query-test")
            }
        }

        assertEquals(exception, actual)
    }
}
