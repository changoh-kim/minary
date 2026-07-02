package kr.co.data.feature.search.repository

import kotlinx.coroutines.flow.first
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.fake.FakeSearchLocalDataSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SearchRepositoryImplTest : BaseDataUnitTest() {
    @Test
    fun `getRecentSearches returns local source flow`() = runDataTest {
        val source = FakeSearchLocalDataSource().apply {
            recentSearches.value = listOf("query-test")
        }
        val repository = SearchRepositoryImpl(source.mock)

        assertEquals(listOf("query-test"), repository.getRecentSearches().first())
    }

    @Test
    fun `addRecentSearch forwards query and timestamp`() = runDataTest {
        val source = FakeSearchLocalDataSource()
        val repository = SearchRepositoryImpl(source.mock)

        repository.addRecentSearch("query-test", 100L)

        assertEquals("query-test", source.addedSearches.single().query)
        assertEquals(100L, source.addedSearches.single().timestamp)
    }

    @Test
    fun `removeRecentSearch forwards query`() = runDataTest {
        val source = FakeSearchLocalDataSource()
        val repository = SearchRepositoryImpl(source.mock)

        repository.removeRecentSearch("query-test")

        assertEquals(listOf("query-test"), source.removedQueries)
    }

    @Test
    fun `clearAll forwards clear command`() = runDataTest {
        val source = FakeSearchLocalDataSource()
        val repository = SearchRepositoryImpl(source.mock)

        repository.clearAll()

        assertEquals(1, source.clearAllCallCount)
    }

    @Test
    fun `command exceptions propagate because search repository has no AppResult contract`() {
        val source = FakeSearchLocalDataSource().apply {
            failure = IllegalStateException("failure-test")
        }
        val repository = SearchRepositoryImpl(source.mock)

        assertThrows(IllegalStateException::class.java) {
            runDataTest { repository.addRecentSearch("query-test", 100L) }
        }
    }
}
