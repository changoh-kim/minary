package kr.co.data.feature.diary.sync

import kotlinx.coroutines.tasks.await
import kr.co.core.common.model.Emotion
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.common.state.SyncStatus
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiaryDto
import kr.co.data.feature.diary.model.DiaryDto
import kr.co.data.testing.AndroidDataFixtures
import kr.co.data.testing.AndroidFakeDiaryLocalDataSource
import kr.co.data.testing.AndroidFakeServerTimeProvider
import kr.co.data.testing.AndroidFakeUserSyncDataStoreProvider
import kr.co.data.testing.BaseFirebaseEmulatorTest
import kr.co.data.testing.assertAndroidOk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.ZoneOffset

class FirestoreDiarySyncManagerEmulatorTest : BaseFirebaseEmulatorTest() {

    @Test
    fun performImmediatePush_pushes_pending_create_and_marks_local_as_synced() =
        runDataAndroidTest {
            val user = createSignedInEmulatorUser()
            val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
            val local = AndroidFakeDiaryLocalDataSource()
            val pendingDiary = AndroidDataFixtures.diaryEntry(
                id = "pending-create-diary-id-test",
                createdAt = CREATED_AT_IN_MONTH,
                lastModifiedAt = 300L,
                syncStatus = DiarySyncStatus.PENDING_CREATE,
            )
            local.seed(pendingDiary)
            local.pendingRelations = listOf(pendingDiary)
            val manager = manager(local, firestoreProvider)

            manager.performImmediatePush(user.uid).assertAndroidOk(Unit)

            val snapshot = firestoreProvider
                .getDiariesRef(user.uid)
                .document("pending-create-diary-id-test")
                .get()
                .await()
            assertTrue(snapshot.exists())
            assertEquals("pending-create-diary-id-test", snapshot.getString(DiaryDto.ID))
            assertEquals(AndroidDataFixtures.TITLE, snapshot.getString(DiaryDto.TITLE))
            assertEquals(300L, snapshot.getLong(DiaryDto.LAST_MODIFIED_AT))
            assertEquals(listOf("pending-create-diary-id-test"), local.markedAsSyncedIds)
        }

    @Test
    fun performImmediatePush_deletes_pending_delete_remote_and_local() = runDataAndroidTest {
        val user = createSignedInEmulatorUser()
        val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
        val local = AndroidFakeDiaryLocalDataSource()
        val pendingDelete = AndroidDataFixtures.diaryEntry(
            id = "pending-delete-diary-id-test",
            createdAt = CREATED_AT_IN_MONTH,
            lastModifiedAt = 300L,
            syncStatus = DiarySyncStatus.PENDING_DELETE,
        )
        local.seed(pendingDelete)
        local.pendingRelations = listOf(pendingDelete)
        seedRemoteDiary(firestoreProvider, user.uid, pendingDelete.toDiaryDto())
        val manager = manager(local, firestoreProvider)

        manager.performImmediatePush(user.uid).assertAndroidOk(Unit)

        val snapshot = firestoreProvider
            .getDiariesRef(user.uid)
            .document("pending-delete-diary-id-test")
            .get()
            .await()
        assertFalse(snapshot.exists())
        assertEquals(listOf("pending-delete-diary-id-test"), local.deletedIds)
    }

    @Test
    fun performMonthSync_pulls_remote_diary_and_updates_metadata() = runDataAndroidTest {
        val user = createSignedInEmulatorUser()
        val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
        val local = AndroidFakeDiaryLocalDataSource()
        val remote = diaryDto(
            id = "month-remote-diary-id-test",
            title = "remote-title-test",
            lastModifiedAt = 300L,
        )
        seedRemoteDiary(firestoreProvider, user.uid, remote)
        val manager = manager(local, firestoreProvider)

        manager.performMonthSync(user.uid, YEAR_MONTH).assertAndroidOk(Unit)

        val upserted = local.upserted.single()
        assertEquals("month-remote-diary-id-test", upserted.diary.id)
        assertEquals("remote-title-test", upserted.diary.title)
        assertEquals(DiarySyncStatus.SYNCED, upserted.diary.syncStatus)
        assertEquals(
            listOf(SyncStatus.LOADING, SyncStatus.SYNCED),
            local.syncMetadataUpdates.map { it.status },
        )
        assertEquals(listOf(YEAR_MONTH, YEAR_MONTH), local.syncMetadataUpdates.map { it.yearMonth })
    }

    @Test
    fun performMonthSync_skips_remote_diary_when_local_is_newer() = runDataAndroidTest {
        val user = createSignedInEmulatorUser()
        val firestoreProvider = FirebaseFirestoreProvider(firebaseFirestore)
        val local = AndroidFakeDiaryLocalDataSource()
        val localDiary = AndroidDataFixtures.diaryEntry(
            id = "month-lww-diary-id-test",
            createdAt = CREATED_AT_IN_MONTH,
            lastModifiedAt = 500L,
            syncStatus = DiarySyncStatus.SYNCED,
        )
        local.seed(localDiary)
        seedRemoteDiary(
            firestoreProvider = firestoreProvider,
            uid = user.uid,
            diary = diaryDto(
                id = "month-lww-diary-id-test",
                title = "older-remote-title-test",
                lastModifiedAt = 300L,
            ),
        )
        val manager = manager(local, firestoreProvider)

        manager.performMonthSync(user.uid, YEAR_MONTH).assertAndroidOk(Unit)

        assertEquals(emptyList<Any>(), local.upserted)
        assertEquals(
            listOf(SyncStatus.LOADING, SyncStatus.SYNCED),
            local.syncMetadataUpdates.map { it.status },
        )
    }

    private fun manager(
        local: AndroidFakeDiaryLocalDataSource,
        firestoreProvider: FirebaseFirestoreProvider,
    ) = FirestoreDiarySyncManager(
        diaryLocalDataSource = local.mock,
        userDataStoreProvider = AndroidFakeUserSyncDataStoreProvider().mock,
        firebaseFirestoreProvider = firestoreProvider,
        serverTime = AndroidFakeServerTimeProvider(now = SERVER_NOW),
    )

    private suspend fun seedRemoteDiary(
        firestoreProvider: FirebaseFirestoreProvider,
        uid: String,
        diary: DiaryDto,
    ) {
        firestoreProvider
            .getDiariesRef(uid)
            .document(diary.id)
            .set(diary)
            .await()
    }

    private fun diaryDto(
        id: String,
        title: String,
        lastModifiedAt: Long,
    ): DiaryDto =
        DiaryDto(
            id = id,
            date = YEAR_MONTH.atDay(15).toString(),
            title = title,
            content = "content-test",
            emotions = listOf(Emotion.JOY.name),
            imageUrls = listOf("image-url-test"),
            createdAt = CREATED_AT_IN_MONTH,
            lastModifiedAt = lastModifiedAt,
        )

    private companion object {
        val YEAR_MONTH: YearMonth = YearMonth.of(2026, 6)
        val CREATED_AT_IN_MONTH: Long =
            YEAR_MONTH.atDay(15).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        const val SERVER_NOW = 1_800_000_000_000L
    }
}
