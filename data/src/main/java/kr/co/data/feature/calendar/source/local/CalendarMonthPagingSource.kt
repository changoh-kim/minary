package kr.co.data.feature.calendar.source.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.michaelbull.result.get
import kotlinx.coroutines.flow.first
import kr.co.data.feature.calendar.model.CalendarDayModel
import kr.co.data.feature.calendar.model.CalendarMonthModel
import kr.co.domain.feature.calendar.generator.CalendarGenerator
import kr.co.core.common.state.SyncStatus
import kr.co.domain.feature.diary.repository.DiaryRepository
import kr.co.domain.feature.session.repository.SessionRepository
import java.time.YearMonth

class CalendarMonthPagingSource(
    private val calendarGenerator: CalendarGenerator,
    private val sessionRepository: SessionRepository,
    private val diaryRepository: DiaryRepository,
) : PagingSource<YearMonth, CalendarMonthModel>() {

    override fun getRefreshKey(state: PagingState<YearMonth, CalendarMonthModel>): YearMonth? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plusMonths(1) ?: anchorPage?.nextKey?.minusMonths(1)
        }
    }

    override suspend fun load(params: LoadParams<YearMonth>): LoadResult<YearMonth, CalendarMonthModel> {
        return try {
            val startYearMonth =
                YearMonth.of(CalendarGenerator.START_YEAR_1902, CalendarGenerator.START_MONTH_1)
            val currentYearMonth = YearMonth.now()
            val targetYearMonth = params.key ?: currentYearMonth
            val loadSize = params.loadSize

            val calendarMonthList = ArrayList<CalendarMonthModel>(loadSize)

            var firstMonth: YearMonth? = null
            var lastMonth: YearMonth? = null

            for (i in 0 until loadSize) {
                val yearMonth = targetYearMonth.minusMonths(i.toLong())

                if (yearMonth.isBefore(startYearMonth)) break
                if (yearMonth.isAfter(currentYearMonth)) continue

                if (firstMonth == null) firstMonth = yearMonth
                lastMonth = yearMonth

                val calendarMonth = calendarGenerator.generateMonth(yearMonth)
                val startDate = calendarMonth.days.first().date
                val endDate = calendarMonth.days.last().date

                val diaries = diaryRepository.getDiariesByDateRange(startDate, endDate)
                    .get()
                    ?.associateBy { it.date } ?: emptyMap()

                val syncStatus = diaryRepository.getSyncStatusStream(yearMonth).first()

                // Trigger sync if IDLE or FAILED (optional, as per plan)
                // IDLE 또는 실패한 경우 동기화 트리거(선택 사항, 계획에 따라)
                if (syncStatus == SyncStatus.IDLE) {
                    val user = sessionRepository.getCurrentUser().get()
                    user?.let { diaryRepository.requestMonthSync(it.uid, yearMonth) }
                }

                val combinedDays = calendarMonth.days.map { day ->
                    CalendarDayModel(
                        date = day.date,
                        isCurrentMonth = day.isCurrentMonth,
                        diary = diaries[day.date]
                    )
                }

                calendarMonthList.add(
                    CalendarMonthModel(
                        yearMonth = yearMonth,
                        days = combinedDays,
                        syncStatus = syncStatus
                    )
                )
            }

            if (calendarMonthList.isEmpty()) {
                return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
            }

            val prevKey =
                if (firstMonth == null || firstMonth >= currentYearMonth) null
                else targetYearMonth.plusMonths(loadSize.toLong())

            val nextKey =
                if (lastMonth == null || lastMonth <= startYearMonth) null
                else targetYearMonth.minusMonths(loadSize.toLong())

            LoadResult.Page(
                data = calendarMonthList,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}