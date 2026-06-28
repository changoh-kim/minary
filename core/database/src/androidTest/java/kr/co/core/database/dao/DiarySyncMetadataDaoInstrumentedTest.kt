package kr.co.core.database.dao

import androidx.room.Room
import kotlinx.coroutines.flow.first
import kr.co.core.common.state.SyncStatus
import kr.co.core.database.database.UserDatabase
import kr.co.core.database.entity.DiarySyncMetadataEntity
import kr.co.core.database.testing.BaseInstrumentationTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.YearMonth

class DiarySyncMetadataDaoInstrumentedTest : BaseInstrumentationTest() {

    private lateinit var database: UserDatabase
    private lateinit var dao: DiarySyncMetadataDao

    @Before
    fun setUpDatabase() {
        database = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.diarySyncMetadataDao()
    }

    @After
    fun tearDownDatabase() {
        database.close()
    }

    @Test
    fun metadataFlowEmitsInsertedMetadata() {
        runCoreAndroidTest {
            val yearMonth = YearMonth.of(2026, 6)
            assertNull(dao.getMetadataFlow(yearMonth).first())

            val expected = DiarySyncMetadataEntity(
                yearMonth = yearMonth,
                status = SyncStatus.SYNCED,
                lastSyncedAt = 100L,
            )
            dao.insertMetadata(expected)

            assertEquals(expected, dao.getMetadataFlow(yearMonth).first())
        }
    }
}
