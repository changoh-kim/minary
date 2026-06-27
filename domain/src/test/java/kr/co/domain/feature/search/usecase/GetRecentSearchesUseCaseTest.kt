package kr.co.domain.feature.search.usecase

import kotlinx.coroutines.flow.first
import kr.co.domain.testing.DomainCoroutineTest
import kr.co.domain.testing.fake.FakeSearchRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetRecentSearchesUseCaseTest : DomainCoroutineTest() {

    @Test
    fun `returns recent searches from repository`() {
        runDomainTest {
            val repository = FakeSearchRepository(initialSearches = listOf("query-test"))
            val useCase = GetRecentSearchesUseCase(repository)

            val result = useCase().first()

            assertEquals(listOf("query-test"), result)
        }
    }
}
