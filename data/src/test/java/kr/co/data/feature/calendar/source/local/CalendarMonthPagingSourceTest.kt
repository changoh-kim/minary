package kr.co.data.feature.calendar.source.local

import androidx.paging.PagingSource
import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kr.co.core.common.state.SyncStatus
import kr.co.data.feature.calendar.model.CalendarMonthModel
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.domain.feature.calendar.generator.CalendarGenerator
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.session.repository.SessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.YearMonth

class CalendarMonthPagingSourceTest : BaseDataUnitTest() {
    @Test
    fun `load merges diaries and requests month sync when status is idle`() = runDataTest {
        val targetMonth = YearMonth.now().minusMonths(1)
        val previousMonth = targetMonth.minusMonths(1)
        val diary = DataFixtures.diary.copy(date = targetMonth.atDay(1))
        val diaryRepository = diaryRepository(
            syncStatusByMonth = mapOf(
                targetMonth to SyncStatus.IDLE,
                previousMonth to SyncStatus.SYNCED,
            ),
            diaries = listOf(diary),
        )
        val sessionRepository = sessionRepository()
        val source = CalendarMonthPagingSource(
            calendarGenerator = CalendarGenerator(),
            sessionRepository = sessionRepository,
            diaryRepository = diaryRepository,
        )

        val page = source.load(
            PagingSource.LoadParams.Refresh(
                key = targetMonth,
                loadSize = 2,
                placeholdersEnabled = false,
            )
        ).assertPage()

        assertEquals(listOf(targetMonth, previousMonth), page.data.map { it.yearMonth })
        assertEquals(SyncStatus.IDLE, page.data.first().syncStatus)
        assertEquals(
            diary,
            page.data.first().days.single { it.date == targetMonth.atDay(1) }.diary,
        )
        coVerify(exactly = 1) {
            diaryRepository.requestMonthSync(DataFixtures.UID, targetMonth)
        }
    }

    @Test
    fun `load does not request month sync when status is already synced`() = runDataTest {
        val targetMonth = YearMonth.now().minusMonths(1)
        val diaryRepository = diaryRepository(
            syncStatusByMonth = mapOf(targetMonth to SyncStatus.SYNCED),
        )
        val sessionRepository = mockk<SessionRepository>()
        val source = CalendarMonthPagingSource(
            calendarGenerator = CalendarGenerator(),
            sessionRepository = sessionRepository,
            diaryRepository = diaryRepository,
        )

        val page = source.load(
            PagingSource.LoadParams.Refresh(
                key = targetMonth,
                loadSize = 1,
                placeholdersEnabled = false,
            )
        ).assertPage()

        assertEquals(listOf(targetMonth), page.data.map { it.yearMonth })
        assertEquals(SyncStatus.SYNCED, page.data.single().syncStatus)
        verify(exactly = 0) { sessionRepository.getSessionStateStream() }
        coVerify(exactly = 0) { sessionRepository.getCurrentUser() }
        coVerify(exactly = 0) { diaryRepository.requestMonthSync(any(), any()) }
    }

    @Test
    fun `load stops at start month and has no next key`() = runDataTest {
        val targetMonth = YearMonth.of(CalendarGenerator.START_YEAR_1902, 2)
        val diaryRepository = diaryRepository(
            syncStatusByMonth = mapOf(
                targetMonth to SyncStatus.SYNCED,
                targetMonth.minusMonths(1) to SyncStatus.SYNCED,
            ),
        )
        val source = CalendarMonthPagingSource(
            calendarGenerator = CalendarGenerator(),
            sessionRepository = sessionRepository(),
            diaryRepository = diaryRepository,
        )

        val page = source.load(
            PagingSource.LoadParams.Refresh(
                key = targetMonth,
                loadSize = 3,
                placeholdersEnabled = false,
            )
        ).assertPage()

        assertEquals(
            listOf(targetMonth, targetMonth.minusMonths(1)),
            page.data.map { it.yearMonth },
        )
        assertEquals(targetMonth.plusMonths(3), page.prevKey)
        assertNull(page.nextKey)
    }

    private fun diaryRepository(
        syncStatusByMonth: Map<YearMonth, SyncStatus>,
        diaries: List<Diary> = emptyList(),
    ): DiaryRepository =
        mockk {
            coEvery { getDiariesByDateRange(any(), any()) } coAnswers {
                val startDate = firstArg<java.time.LocalDate>()
                val endDate = secondArg<java.time.LocalDate>()
                Ok(
                    diaries.filter { diary ->
                        !diary.date.isBefore(startDate) && !diary.date.isAfter(endDate)
                    }
                )
            }
            every { getSyncStatusStream(any()) } answers {
                flowOf(syncStatusByMonth[firstArg()] ?: SyncStatus.SYNCED)
            }
            coEvery { requestMonthSync(any(), any()) } returns Ok(Unit)
        }

    private fun sessionRepository(): SessionRepository =
        mockk {
            coEvery { getCurrentUser() } returns Ok(DataFixtures.userSession)
        }

    private fun PagingSource.LoadResult<YearMonth, CalendarMonthModel>.assertPage():
        PagingSource.LoadResult.Page<YearMonth, CalendarMonthModel> =
        this as? PagingSource.LoadResult.Page
            ?: throw AssertionError("Expected Page but was $this")
}
