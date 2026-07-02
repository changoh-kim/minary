package kr.co.data.feature.diary.source.local

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kr.co.core.common.model.Emotion
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.common.state.SyncStatus
import kr.co.core.database.entity.DiarySyncMetadataEntity
import kr.co.data.testing.AndroidDataFixtures
import kr.co.data.testing.BaseRoomLocalDataSourceTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryLocalDataSourceInstrumentedTest : BaseRoomLocalDataSourceTest() {
    private lateinit var source: DiaryLocalDataSource

    @Before
    fun setUpSource() {
        source = DiaryLocalDataSource(databaseProvider)
    }

    @Test
    fun insert_and_getDiaryWithRelations_persists_relation_rows() = runDataAndroidTest {
        val entry = AndroidDataFixtures.diaryEntry()

        source.insert(entry)

        val actual = source.getDiaryWithRelations(AndroidDataFixtures.date)
        assertEquals(entry.diary, actual?.diary)
        assertEquals(setOf(Emotion.JOY, Emotion.CALMNESS), actual?.emotions?.map { it.emotion }?.toSet())
        assertEquals(listOf(AndroidDataFixtures.IMAGE_URL), actual?.imageUrls?.map { it.imageUrl })
    }

    @Test
    fun update_replaces_emotions_and_image_urls() = runDataAndroidTest {
        val original = AndroidDataFixtures.diaryEntry()
        val updated = AndroidDataFixtures.diaryEntry(
            title = "updated-title-test",
            emotions = listOf(Emotion.SADNESS),
            imageUrls = listOf("updated-image-url-test"),
        )
        source.insert(original)

        source.update(updated)

        val actual = source.getDiaryWithRelations(AndroidDataFixtures.DIARY_ID)
        assertEquals("updated-title-test", actual?.diary?.title)
        assertEquals(listOf(Emotion.SADNESS), actual?.emotions?.map { it.emotion })
        assertEquals(listOf("updated-image-url-test"), actual?.imageUrls?.map { it.imageUrl })
    }

    @Test
    fun getDiaryStream_emits_inserted_diary() = runDataAndroidTest {
        val entry = AndroidDataFixtures.diaryEntry()
        val emittedDiary = async {
            source.getDiaryStream(AndroidDataFixtures.date)
                .filterNotNull()
                .first()
        }
        runCurrent()

        source.insert(entry)

        assertEquals(AndroidDataFixtures.DIARY_ID, emittedDiary.await().id)
    }

    @Test
    fun deleteOldDiaries_removes_only_old_synced_items() = runDataAndroidTest {
        val oldSynced = AndroidDataFixtures.diaryEntry(
            id = "old-synced-diary-id-test",
            date = LocalDate.of(2025, 1, 1),
            lastModifiedAt = 10L,
            syncStatus = DiarySyncStatus.SYNCED,
        )
        val oldPending = AndroidDataFixtures.diaryEntry(
            id = "old-pending-diary-id-test",
            date = LocalDate.of(2025, 1, 2),
            lastModifiedAt = 20L,
            syncStatus = DiarySyncStatus.PENDING_UPDATE,
        )
        val recentSynced = AndroidDataFixtures.diaryEntry(
            id = "recent-synced-diary-id-test",
            date = LocalDate.of(2026, 1, 1),
            lastModifiedAt = 200L,
            syncStatus = DiarySyncStatus.SYNCED,
        )
        source.insert(oldSynced)
        source.insert(oldPending)
        source.insert(recentSynced)

        source.deleteOldDiaries(cutoff = 100L)

        assertNull(source.getDiary("old-synced-diary-id-test"))
        assertNotNull(source.getDiary("old-pending-diary-id-test"))
        assertNotNull(source.getDiary("recent-synced-diary-id-test"))
    }

    @Test
    fun getPendingDiariesWithRelations_orders_by_lastModifiedAt_and_applies_limit() =
        runDataAndroidTest {
            val pendingNewest = AndroidDataFixtures.diaryEntry(
                id = "pending-newest-diary-id-test",
                date = LocalDate.of(2026, 1, 3),
                lastModifiedAt = 300L,
                syncStatus = DiarySyncStatus.PENDING_UPDATE,
            )
            val pendingOldest = AndroidDataFixtures.diaryEntry(
                id = "pending-oldest-diary-id-test",
                date = LocalDate.of(2026, 1, 1),
                lastModifiedAt = 100L,
                syncStatus = DiarySyncStatus.PENDING_CREATE,
            )
            val pendingMiddle = AndroidDataFixtures.diaryEntry(
                id = "pending-middle-diary-id-test",
                date = LocalDate.of(2026, 1, 2),
                lastModifiedAt = 200L,
                syncStatus = DiarySyncStatus.PENDING_DELETE,
            )
            val synced = AndroidDataFixtures.diaryEntry(
                id = "synced-diary-id-test",
                date = LocalDate.of(2026, 1, 4),
                lastModifiedAt = 50L,
                syncStatus = DiarySyncStatus.SYNCED,
            )
            source.insert(pendingNewest)
            source.insert(pendingOldest)
            source.insert(pendingMiddle)
            source.insert(synced)

            val actual = source.getPendingDiariesWithRelations(limit = 2).map { it.diary.id }

            assertEquals(
                listOf("pending-oldest-diary-id-test", "pending-middle-diary-id-test"),
                actual,
            )
            assertEquals(3, source.getPendingItemCount())
        }

    @Test
    fun markAsSynced_updates_sync_status() = runDataAndroidTest {
        source.insert(AndroidDataFixtures.diaryEntry(syncStatus = DiarySyncStatus.PENDING_UPDATE))

        source.markAsSynced(AndroidDataFixtures.DIARY_ID)

        assertEquals(DiarySyncStatus.SYNCED, source.getSyncStatus(AndroidDataFixtures.DIARY_ID))
    }

    @Test
    fun syncMetadataStream_returns_updated_metadata() = runDataAndroidTest {
        val yearMonth = YearMonth.of(2026, 6)
        val metadata = DiarySyncMetadataEntity(
            yearMonth = yearMonth,
            status = SyncStatus.SYNCED,
            lastSyncedAt = 300L,
        )
        assertNull(source.getSyncMetadataStream(yearMonth).first())

        source.updateSyncMetadata(metadata)

        assertEquals(metadata, source.getSyncMetadataStream(yearMonth).first())
    }
}
