package kr.co.data.feature.dashboard.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kr.co.core.common.error.DomainError
import kr.co.core.common.model.Emotion
import kr.co.core.database.dao.DiaryDao
import kr.co.core.database.database.UserDatabase
import kr.co.core.database.model.DiaryWithRelations
import kr.co.core.database.model.EmotionStats
import kr.co.core.database.provider.UserDatabaseProvider
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.assertErr
import kr.co.data.testing.assertOk
import kr.co.data.testing.fake.FakeAppLogger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DashboardRepositoryImplTest : BaseDataUnitTest() {
    @Test
    fun `getDashboard combines statistics fills heatmap window and calculates longest streak`() = runDataTest {
        val today = LocalDate.now()
        val todayDiary = diaryWithDate(id = "today-diary-id-test", date = today)
        val yesterdayDiary = diaryWithDate(id = "yesterday-diary-id-test", date = today.minusDays(1))
        val diaryDao = mockDiaryDao {
            coEvery {
                getDiariesByDateRangeWithRelations(
                    today.minusDays(DashboardRepositoryImpl.HEATMAP_SIZE),
                    today,
                )
            } returns listOf(yesterdayDiary, todayDiary)
            coEvery { getTotalDiaryCount() } returns 7
            coEvery { getDiaryCountByDateRange(today.with(DayOfWeek.MONDAY), today) } returns 2
            coEvery { getTotalWordCount() } returns 123
            coEvery { getEmotionStatistics() } returns listOf(
                EmotionStats(Emotion.JOY, 3),
                EmotionStats(Emotion.CALMNESS, 1),
            )
            coEvery { getAllDiaryDates() } returns listOf(
                today,
                today.minusDays(1),
                today.minusDays(2),
                today.minusDays(5),
            )
        }
        val repository = repository(diaryDao = diaryDao)

        val actual = repository.getDashboard().assertOk()

        assertEquals(DashboardRepositoryImpl.HEATMAP_SIZE.toInt(), actual.recentDiaries.size)
        assertEquals(today.minusDays(1), actual.recentDiaries[88]?.date)
        assertEquals(today, actual.recentDiaries[89]?.date)
        assertEquals(7, actual.totalDiaryCount)
        assertEquals(2, actual.weeklyDiaryCount)
        assertEquals(123, actual.totalWordCount)
        assertEquals(3, actual.longestStreak)
        assertEquals(mapOf(Emotion.JOY to 3, Emotion.CALMNESS to 1), actual.emotionCounts)
    }

    @Test
    fun `getDashboard maps dao exception to unexpected and logs failure`() = runDataTest {
        val logger = FakeAppLogger()
        val failure = IllegalStateException("failure-test")
        val diaryDao = mockDiaryDao {
            coEvery { getDiariesByDateRangeWithRelations(any(), any()) } throws failure
        }
        val repository = repository(logger = logger, diaryDao = diaryDao)

        repository.getDashboard().assertErr(DomainError.Unexpected)

        assertEquals(failure, logger.errorThrowables.single().first)
        assertEquals("Failed to get dashboard", logger.errorThrowables.single().second)
    }

    private fun repository(
        logger: FakeAppLogger = FakeAppLogger(),
        diaryDao: DiaryDao = mockDiaryDao(),
    ) = DashboardRepositoryImpl(
        logger = logger,
        databaseProvider = mockDatabaseProvider(diaryDao),
    )

    private fun mockDatabaseProvider(diaryDao: DiaryDao): UserDatabaseProvider {
        val database = mockk<UserDatabase> {
            every { diaryDao() } returns diaryDao
        }
        return mockk {
            every { getDatabase() } returns database
        }
    }

    private fun mockDiaryDao(stub: DiaryDao.() -> Unit = {}): DiaryDao =
        mockk<DiaryDao>().also(stub)

    private fun diaryWithDate(id: String, date: LocalDate): DiaryWithRelations =
        DataFixtures.diaryWithRelations.copy(
            diary = DataFixtures.diaryEntity.copy(
                id = id,
                date = date,
            ),
        )
}
