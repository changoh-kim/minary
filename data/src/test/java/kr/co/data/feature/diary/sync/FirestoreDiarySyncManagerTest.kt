package kr.co.data.feature.diary.sync

import io.mockk.mockk
import kr.co.core.common.state.DiarySyncStatus
import kr.co.core.datastore.sync.UserSyncDataStoreProvider
import kr.co.core.firebase.provider.FirebaseFirestoreProvider
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.fake.FakeDiaryLocalDataSource
import kr.co.data.testing.fake.FakeServerTimeProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FirestoreDiarySyncManagerTest : BaseDataUnitTest() {
    @Test
    fun `pullRemoteDiaries upserts remote diary when local is missing`() = runDataTest {
        val local = FakeDiaryLocalDataSource()
        val manager = manager(local)
        val remote = DataFixtures.diaryDto.copy(
            id = "remote-diary-id-test",
            lastModifiedAt = 300L,
        )

        val actual = manager.pullRemoteDiaries(listOf(remote))

        assertEquals(300L, actual)
        assertEquals("remote-diary-id-test", local.upserted.single().diary.id)
        assertEquals(DiarySyncStatus.SYNCED, local.upserted.single().diary.syncStatus)
    }

    @Test
    fun `pullRemoteDiaries upserts remote diary when remote is newer than local`() = runDataTest {
        val local = FakeDiaryLocalDataSource().apply {
            seed(
                DataFixtures.diaryWithRelations.copy(
                    diary = DataFixtures.diaryEntity.copy(lastModifiedAt = 100L),
                )
            )
        }
        val manager = manager(local)
        val remote = DataFixtures.diaryDto.copy(lastModifiedAt = 300L)

        val actual = manager.pullRemoteDiaries(listOf(remote))

        assertEquals(300L, actual)
        assertEquals(DataFixtures.DIARY_ID, local.upserted.single().diary.id)
        assertEquals(300L, local.upserted.single().diary.lastModifiedAt)
        assertEquals(DiarySyncStatus.SYNCED, local.upserted.single().diary.syncStatus)
    }

    @Test
    fun `pullRemoteDiaries skips remote diary when local is newer`() = runDataTest {
        val local = FakeDiaryLocalDataSource().apply {
            seed(
                DataFixtures.diaryWithRelations.copy(
                    diary = DataFixtures.diaryEntity.copy(lastModifiedAt = 500L),
                )
            )
        }
        val manager = manager(local)
        val remote = DataFixtures.diaryDto.copy(lastModifiedAt = 300L)

        val actual = manager.pullRemoteDiaries(listOf(remote))

        assertEquals(300L, actual)
        assertEquals(emptyList<Any>(), local.upserted)
    }

    @Test
    fun `pullRemoteDiaries returns max remote lastModifiedAt across batch`() = runDataTest {
        val local = FakeDiaryLocalDataSource()
        val manager = manager(local)
        val older = DataFixtures.diaryDto.copy(
            id = "older-remote-diary-id-test",
            lastModifiedAt = 100L,
        )
        val newer = DataFixtures.diaryDto.copy(
            id = "newer-remote-diary-id-test",
            lastModifiedAt = 400L,
        )

        val actual = manager.pullRemoteDiaries(listOf(older, newer))

        assertEquals(400L, actual)
        assertEquals(
            listOf("older-remote-diary-id-test", "newer-remote-diary-id-test"),
            local.upserted.map { it.diary.id },
        )
    }

    private fun manager(local: FakeDiaryLocalDataSource) = FirestoreDiarySyncManager(
        diaryLocalDataSource = local.mock,
        userDataStoreProvider = mockk<UserSyncDataStoreProvider>(),
        firebaseFirestoreProvider = mockk<FirebaseFirestoreProvider>(),
        serverTime = FakeServerTimeProvider(now = DataFixtures.UPDATED_AT),
    )
}
