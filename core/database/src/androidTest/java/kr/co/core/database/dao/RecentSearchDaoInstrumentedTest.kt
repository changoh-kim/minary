package kr.co.core.database.dao

import androidx.room.Room
import kotlinx.coroutines.flow.first
import kr.co.core.database.database.UserDatabase
import kr.co.core.database.entity.RecentSearchEntity
import kr.co.core.database.testing.BaseInstrumentationTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RecentSearchDaoInstrumentedTest : BaseInstrumentationTest() {

    private lateinit var database: UserDatabase
    private lateinit var dao: RecentSearchDao

    @Before
    fun setUpDatabase() {
        database = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.recentSearchDao()
    }

    @After
    fun tearDownDatabase() {
        database.close()
    }

    @Test
    fun addRecentSearchWithLimit_keepsTenNewestQueries() {
        runCoreAndroidTest {
            (1..11).forEach { index ->
                dao.addRecentSearchWithLimit(
                    RecentSearchEntity(
                        query = "query-$index-test",
                        timestamp = index.toLong(),
                    )
                )
            }

            val actual = dao.getRecentSearches().first()

            assertEquals(10, actual.size)
            assertEquals("query-11-test", actual.first())
            assertEquals("query-2-test", actual.last())
        }
    }

    @Test
    fun deleteAndClearUpdateRecentSearchFlow() {
        runCoreAndroidTest {
            dao.addRecentSearchWithLimit(RecentSearchEntity("query-test", 2L))
            dao.addRecentSearchWithLimit(RecentSearchEntity("other-query-test", 1L))

            dao.deleteRecentSearch("query-test")
            assertEquals(listOf("other-query-test"), dao.getRecentSearches().first())

            dao.clearAll()
            assertEquals(emptyList<String>(), dao.getRecentSearches().first())
        }
    }
}
