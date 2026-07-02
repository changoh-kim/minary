package kr.co.data.feature.search.source

import kotlinx.coroutines.flow.first
import kr.co.core.database.entity.RecentSearchEntity
import kr.co.data.testing.BaseRoomLocalDataSourceTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchLocalDataSourceInstrumentedTest : BaseRoomLocalDataSourceTest() {
    private lateinit var source: SearchLocalDataSource

    @Before
    fun setUpSource() {
        source = SearchLocalDataSource(databaseProvider)
    }

    @Test
    fun addRecentSearch_keeps_latest_ten_queries_in_descending_order() = runDataAndroidTest {
        (0..11).forEach { index ->
            source.addRecentSearch(
                RecentSearchEntity(
                    query = "query-$index-test",
                    timestamp = index.toLong(),
                )
            )
        }

        assertEquals(
            (11 downTo 2).map { index -> "query-$index-test" },
            source.getRecentSearches().first(),
        )
    }

    @Test
    fun addRecentSearch_replaces_same_query_and_updates_order() = runDataAndroidTest {
        source.addRecentSearch(RecentSearchEntity(query = "query-a-test", timestamp = 1L))
        source.addRecentSearch(RecentSearchEntity(query = "query-b-test", timestamp = 2L))

        source.addRecentSearch(RecentSearchEntity(query = "query-a-test", timestamp = 3L))

        assertEquals(
            listOf("query-a-test", "query-b-test"),
            source.getRecentSearches().first(),
        )
    }

    @Test
    fun removeRecentSearch_and_clearAll_update_recent_search_flow() = runDataAndroidTest {
        source.addRecentSearch(RecentSearchEntity(query = "query-a-test", timestamp = 1L))
        source.addRecentSearch(RecentSearchEntity(query = "query-b-test", timestamp = 2L))

        source.removeRecentSearch("query-a-test")

        assertEquals(listOf("query-b-test"), source.getRecentSearches().first())

        source.clearAll()

        assertEquals(emptyList<String>(), source.getRecentSearches().first())
    }
}
