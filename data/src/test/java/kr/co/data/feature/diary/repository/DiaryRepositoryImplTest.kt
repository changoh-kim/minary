package kr.co.data.feature.diary.repository

import com.github.michaelbull.result.Err
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kr.co.core.common.error.DomainError
import kr.co.core.common.model.Emotion
import kr.co.core.common.state.DiarySyncStatus
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiary
import kr.co.data.feature.diary.mapper.DiaryMapper.toDiaryWithRelations
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.assertErr
import kr.co.data.testing.assertOk
import kr.co.data.testing.fake.FakeAppLogger
import kr.co.data.testing.fake.FakeDiaryLocalDataSource
import kr.co.data.testing.fake.FakeDiarySyncManager
import kr.co.data.testing.fake.FakeDiarySyncScheduler
import kr.co.data.testing.fake.FakeEmotionRemoteDataSource
import kr.co.data.testing.fake.FakeServerTimeProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryRepositoryImplTest : BaseDataUnitTest() {
    @Test
    fun `createDiary analyzes emotions inserts local diary schedules sync and emits change`() = runDataTest {
        val local = FakeDiaryLocalDataSource()
        val emotion = FakeEmotionRemoteDataSource().apply {
            emotions = listOf(Emotion.SADNESS)
        }
        val scheduler = FakeDiarySyncScheduler()
        val repository = repository(local = local, emotion = emotion, scheduler = scheduler)
        val event = async { repository.diaryChangeEvent.first() }
        runCurrent()

        repository.createDiary(DataFixtures.diary).assertOk(Unit)

        assertEquals(listOf(DataFixtures.diary), emotion.analyzedDiaries)
        assertEquals(listOf(Emotion.SADNESS), local.inserted.single().toDiary().emotions)
        assertEquals(listOf("scheduleImmediateSync"), scheduler.calls)
        event.await()
    }

    @Test
    fun `createDiary maps emotion source exception to unexpected and skips local insert`() = runDataTest {
        val local = FakeDiaryLocalDataSource()
        val emotion = FakeEmotionRemoteDataSource().apply {
            failure = IllegalStateException("failure-test")
        }
        val scheduler = FakeDiarySyncScheduler()
        val repository = repository(local = local, emotion = emotion, scheduler = scheduler)

        repository.createDiary(DataFixtures.diary).assertErr(DomainError.Unexpected)

        assertEquals(emptyList<Any>(), local.inserted)
        assertEquals(emptyList<String>(), scheduler.calls)
    }

    @Test
    fun `updateDiary returns diary not found when local sync status is missing`() = runDataTest {
        val local = FakeDiaryLocalDataSource()
        val emotion = FakeEmotionRemoteDataSource()
        val scheduler = FakeDiarySyncScheduler()
        val repository = repository(local = local, emotion = emotion, scheduler = scheduler)

        repository.updateDiary(DataFixtures.diary).assertErr(DomainError.Diary.NotFound)

        assertEquals(emptyList<Any>(), emotion.analyzedDiaries)
        assertEquals(emptyList<Any>(), local.updated)
        assertEquals(emptyList<String>(), scheduler.calls)
    }

    @Test
    fun `updateDiary preserves pending create status`() = runDataTest {
        val pendingCreate = DataFixtures.diary.copy(syncStatus = DiarySyncStatus.PENDING_CREATE)
        val local = FakeDiaryLocalDataSource().apply {
            seed(pendingCreate.toDiaryWithRelations())
        }
        val emotion = FakeEmotionRemoteDataSource().apply {
            emotions = listOf(Emotion.JOY)
        }
        val scheduler = FakeDiarySyncScheduler()
        val repository = repository(local = local, emotion = emotion, scheduler = scheduler)

        val actual = repository.updateDiary(
            pendingCreate.copy(title = "updated-title-test", syncStatus = DiarySyncStatus.PENDING_UPDATE)
        ).assertOk()

        assertEquals(DiarySyncStatus.PENDING_CREATE, actual.syncStatus)
        assertEquals(DiarySyncStatus.PENDING_CREATE, local.updated.single().diary.syncStatus)
        assertEquals(listOf("scheduleImmediateSync"), scheduler.calls)
    }

    @Test
    fun `updateDiary changes synced diary to pending update`() = runDataTest {
        val synced = DataFixtures.diary.copy(syncStatus = DiarySyncStatus.SYNCED)
        val local = FakeDiaryLocalDataSource().apply {
            seed(synced.toDiaryWithRelations())
        }
        val scheduler = FakeDiarySyncScheduler()
        val repository = repository(local = local, scheduler = scheduler)

        val actual = repository.updateDiary(synced.copy(title = "updated-title-test")).assertOk()

        assertEquals(DiarySyncStatus.PENDING_UPDATE, actual.syncStatus)
        assertEquals(DiarySyncStatus.PENDING_UPDATE, local.updated.single().diary.syncStatus)
        assertEquals(listOf("scheduleImmediateSync"), scheduler.calls)
    }

    @Test
    fun `deleteDiary removes pending create without scheduling sync`() = runDataTest {
        val pendingCreate = DataFixtures.diary.copy(syncStatus = DiarySyncStatus.PENDING_CREATE)
        val local = FakeDiaryLocalDataSource().apply {
            seed(pendingCreate.toDiaryWithRelations())
        }
        val scheduler = FakeDiarySyncScheduler()
        val repository = repository(local = local, scheduler = scheduler)

        repository.deleteDiary(pendingCreate).assertOk(Unit)

        assertEquals(listOf(DataFixtures.DIARY_ID), local.deletedIds)
        assertEquals(emptyList<String>(), scheduler.calls)
    }

    @Test
    fun `deleteDiary marks synced diary pending delete and schedules sync`() = runDataTest {
        val synced = DataFixtures.diary.copy(syncStatus = DiarySyncStatus.SYNCED)
        val local = FakeDiaryLocalDataSource().apply {
            seed(synced.toDiaryWithRelations())
        }
        val scheduler = FakeDiarySyncScheduler()
        val repository = repository(local = local, scheduler = scheduler)

        repository.deleteDiary(synced).assertOk(Unit)

        assertEquals(DiarySyncStatus.PENDING_DELETE, local.updated.single().diary.syncStatus)
        assertEquals(listOf("scheduleImmediateSync"), scheduler.calls)
    }

    @Test
    fun `deleteOldDiaries uses server time cutoff`() = runDataTest {
        val local = FakeDiaryLocalDataSource()
        val serverTime = FakeServerTimeProvider(now = 100_000_000_000L)
        val repository = repository(local = local, serverTime = serverTime)

        repository.deleteOldDiaries().assertOk(Unit)

        assertEquals(
            listOf(100_000_000_000L - TimeUnit.DAYS.toMillis(365)),
            local.deletedOldCutoffs,
        )
    }

    @Test
    fun `getDiary maps local relation to domain diary`() = runDataTest {
        val local = FakeDiaryLocalDataSource().apply {
            seed(DataFixtures.diaryWithRelations)
        }
        val repository = repository(local = local)

        repository.getDiary(DataFixtures.date).assertOk(DataFixtures.diary)
    }

    @Test
    fun `requestMonthSync forwards uid and month to sync manager`() = runDataTest {
        val syncManager = FakeDiarySyncManager()
        val repository = repository(syncManager = syncManager)
        val yearMonth = YearMonth.of(2026, 6)

        repository.requestMonthSync(DataFixtures.UID, yearMonth).assertOk(Unit)

        assertEquals(listOf(DataFixtures.UID to yearMonth), syncManager.monthSyncRequests)
    }

    @Test
    fun `requestMonthSync propagates manager AppResult failure`() = runDataTest {
        val syncManager = FakeDiarySyncManager().apply {
            monthSyncResult = Err(DomainError.NetworkUnavailable)
        }
        val repository = repository(syncManager = syncManager)

        repository.requestMonthSync(DataFixtures.UID, YearMonth.of(2026, 6))
            .assertErr(DomainError.NetworkUnavailable)
    }

    private fun repository(
        local: FakeDiaryLocalDataSource = FakeDiaryLocalDataSource(),
        emotion: FakeEmotionRemoteDataSource = FakeEmotionRemoteDataSource().apply {
            emotions = DataFixtures.diary.emotions
        },
        scheduler: FakeDiarySyncScheduler = FakeDiarySyncScheduler(),
        syncManager: FakeDiarySyncManager = FakeDiarySyncManager(),
        serverTime: FakeServerTimeProvider = FakeServerTimeProvider(now = DataFixtures.UPDATED_AT),
    ) = DiaryRepositoryImpl(
        logger = FakeAppLogger(),
        diarySyncManager = syncManager,
        diarySyncScheduler = scheduler,
        localDataSource = local.mock,
        emotionRemoteDataSource = emotion.mock,
        serverTime = serverTime,
    )
}
