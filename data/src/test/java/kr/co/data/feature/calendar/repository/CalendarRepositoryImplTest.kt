package kr.co.data.feature.calendar.repository

import com.github.michaelbull.result.Ok
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kr.co.core.common.state.SyncStatus
import kr.co.data.testing.BaseDataUnitTest
import kr.co.data.testing.DataFixtures
import kr.co.data.testing.collectPagingDataItems
import kr.co.domain.feature.calendar.generator.CalendarGenerator
import kr.co.domain.feature.diary.model.Diary
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.session.repository.SessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

class CalendarRepositoryImplTest : BaseDataUnitTest() {
    @Test
    fun `getYearlyPages emits mapped domain calendar months`() = runDataTest {
        val targetYear = Year.now().minusYears(1)
        val repository = repository(
            sessionRepository = mockk(),
            diaryRepository = diaryRepository(),
        )

        val pagingData = repository.getYearlyPages(targetYear).first()
        val items = collectPagingDataItems(pagingData)

        assertEquals(144, items.size)
        assertEquals(targetYear.minusYears(11).atMonth(1), items.first().yearMonth)
        assertEquals(targetYear.atMonth(12), items.last().yearMonth)
        assertTrue(items.all { it.days.size == 42 })
        assertTrue(items.all { month -> month.days.all { it.diary == null } })
    }

    @Test
    fun `getMonthlyPages emits mapped months and triggers idle month sync`() = runDataTest {
        val targetMonth = YearMonth.now().minusMonths(1)
        val diary = DataFixtures.diary.copy(date = targetMonth.atDay(1))
        val diaryRepository = diaryRepository(
            syncStatusByMonth = mapOf(targetMonth to SyncStatus.IDLE),
            diaries = listOf(diary),
        )
        val repository = repository(
            sessionRepository = sessionRepository(),
            diaryRepository = diaryRepository,
        )

        val pagingData = repository.getMonthlyPages(targetMonth).first()
        val items = collectPagingDataItems(pagingData)

        assertEquals(12, items.size)
        assertEquals(targetMonth, items.first().yearMonth)
        assertEquals(targetMonth.minusMonths(11), items.last().yearMonth)
        assertEquals(SyncStatus.IDLE, items.first().syncStatus)
        assertEquals(
            diary,
            items.first().days.single { it.date == targetMonth.atDay(1) }.diary,
        )
        coVerify(exactly = 1) {
            diaryRepository.requestMonthSync(DataFixtures.UID, targetMonth)
        }
    }

    private fun repository(
        sessionRepository: SessionRepository,
        diaryRepository: DiaryRepository,
    ) = CalendarRepositoryImpl(
        calendarGenerator = CalendarGenerator(),
        sessionRepository = sessionRepository,
        diaryRepository = diaryRepository,
    )

    private fun diaryRepository(
        syncStatusByMonth: Map<YearMonth, SyncStatus> = emptyMap(),
        diaries: List<Diary> = emptyList(),
    ): DiaryRepository =
        mockk {
            coEvery { getDiariesByDateRange(any(), any()) } coAnswers {
                val startDate = firstArg<LocalDate>()
                val endDate = secondArg<LocalDate>()
                Ok(
                    diaries.filter { diary ->
                        !diary.date.isBefore(startDate) && !diary.date.isAfter(endDate)
                    },
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
}
