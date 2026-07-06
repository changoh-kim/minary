package kr.co.core.database.dao

import androidx.room.Room
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kr.co.core.common.model.Emotion
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.database.database.UserDatabase
import kr.co.core.database.testing.BaseInstrumentationTest
import kr.co.core.database.testing.DatabaseFixtures
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class DiaryDaoInstrumentedTest : BaseInstrumentationTest() {

    private lateinit var database: UserDatabase
    private lateinit var dao: DiaryDao

    @Before
    fun setUpDatabase() {
        database = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.diaryDao()
    }

    @After
    fun tearDownDatabase() {
        database.close()
    }

    @Test
    fun insertDiaryWithRelations_storesDiaryEmotionsAndImages() {
        runCoreAndroidTest {
            val relation = DatabaseFixtures.relation(
                emotions = listOf(Emotion.CALMNESS, Emotion.JOY),
                imageUrls = listOf("image-url-1-test", "image-url-2-test"),
            )

            dao.insertDiaryWithRelations(relation)
            val actual = dao.getDiaryWithRelations(DatabaseFixtures.DATE)

            assertNotNull(actual)
            assertEquals(relation.diary, actual!!.diary)
            assertEquals(2, actual.emotions.size)
            assertEquals(2, actual.imageUrls.size)
        }
    }

    @Test
    fun deleteDiary_cascadesRelationRows() {
        runCoreAndroidTest {
            val relation = DatabaseFixtures.relation()
            dao.insertDiaryWithRelations(relation)

            dao.deleteDiary(relation.diary.id)

            assertNull(dao.getDiaryWithRelations(relation.diary.id))
            assertEquals(emptyList<Any>(), dao.getEmotions(relation.diary.id))
            assertEquals(emptyList<Any>(), dao.getImageUrls(relation.diary.id))
        }
    }

    @Test
    fun diaryFlow_emitsWhenRowsChange() {
        runCoreAndroidTest {
            val updatedEmission = async {
                dao.getAllDiariesWithRelationsFlow()
                    .filter { it.isNotEmpty() }
                    .first()
            }

            dao.insertDiaryWithRelations(DatabaseFixtures.relation())

            val actual = updatedEmission.await()
            assertEquals(1, actual.size)
        }
    }

    @Test
    fun queriesExcludePendingDeleteDiaries() {
        runCoreAndroidTest {
            val pendingDelete = DatabaseFixtures.relation(
                diary = DatabaseFixtures.diary(syncStatus = DiarySyncStatus.PENDING_DELETE),
            )
            dao.insertDiaryWithRelations(pendingDelete)

            assertNull(dao.getDiary(DatabaseFixtures.DATE))
            assertNull(dao.getDiaryWithRelations(DatabaseFixtures.DATE))
        }
    }

    @Test
    fun deleteOldDiaries_deletesOnlySyncedRowsBeforeCutoff() {
        runCoreAndroidTest {
            dao.insertDiaryWithRelations(
                DatabaseFixtures.relation(
                    diary = DatabaseFixtures.diary(id = "old-synced-test", lastModifiedAt = 1L),
                )
            )
            dao.insertDiaryWithRelations(
                DatabaseFixtures.relation(
                    diary = DatabaseFixtures.diary(
                        id = "old-pending-test",
                        date = DatabaseFixtures.DATE.plusDays(1),
                        lastModifiedAt = 1L,
                        syncStatus = DiarySyncStatus.PENDING_UPDATE,
                    ),
                )
            )

            val deletedCount = dao.deleteOldDiaries(cutoff = 10L)

            assertEquals(1, deletedCount)
            assertNull(dao.getDiary("old-synced-test"))
            assertNotNull(dao.getDiary("old-pending-test"))
        }
    }
}
