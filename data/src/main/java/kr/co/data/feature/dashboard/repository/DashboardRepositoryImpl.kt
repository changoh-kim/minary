package kr.co.data.feature.dashboard.repository

import android.util.Log
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onErr
import kr.co.data.extension.TAG
import kr.co.data.extension.toDomainError
import kr.co.data.feature.dashboard.mapper.DashboardMapper.toDashboard
import kr.co.data.feature.dashboard.model.DashboardModel
import kr.co.data.feature.diary.source.local.model.DiaryWithRelations
import kr.co.data.local.provider.UserDatabaseProvider
import kr.co.domain.error.DomainError
import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.domain.feature.dashboard.repository.DashboardRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val databaseProvider: UserDatabaseProvider,
) : DashboardRepository {
    companion object {
        const val HEATMAP_SIZE = 90L
    }

    private val diaryDao get() = databaseProvider.getDatabase().diaryDao()

    override suspend fun getDashboard(): Result<Dashboard, DomainError> =
        runSuspendCatching {
            val today = LocalDate.now()
            val startDate = today.minusDays(HEATMAP_SIZE)

            val recentDiaries =
                diaryDao.getDiariesByDateRangeWithRelations(
                    startDate = startDate,
                    endDate = today
                ).fillEmptyDiaries(today).reversed()
            val totalDiaryCount = diaryDao.getTotalDiaryCount()

            val startOfWeek = today.with(java.time.DayOfWeek.MONDAY)
            val weeklyDiaryCount = diaryDao.getDiaryCountByDateRange(startOfWeek, today)

            val totalWordCount = diaryDao.getTotalWordCount()

            val emotionStats = diaryDao.getEmotionStatistics()
            val emotionCounts = emotionStats.associate { it.emotion to it.count }

            val allDates = diaryDao.getAllDiaryDates()
            val longestStreak = calculateLongestStreak(allDates)

            DashboardModel(
                recentDiaries = recentDiaries,
                totalDiaryCount = totalDiaryCount,
                weeklyDiaryCount = weeklyDiaryCount,
                totalWordCount = totalWordCount,
                longestStreak = longestStreak,
                emotionCounts = emotionCounts,
            ).toDashboard()
        }
        .onErr { Log.e(TAG, "Failed to get dashboard", it) }
        .mapError { it.toDomainError() }

    private fun calculateLongestStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val sortedDates = dates.distinct().sortedDescending()
        var maxStreak = 0
        var currentStreak = 0
        var lastDate: LocalDate? = null

        for (date in sortedDates) {
            if (lastDate == null || lastDate.minusDays(1) == date) {
                currentStreak++
            } else {
                maxStreak = maxOf(maxStreak, currentStreak)
                currentStreak = 1
            }
            lastDate = date
        }
        return maxOf(maxStreak, currentStreak)
    }

    /**
     * 기준일(today)로부터 지정된 일수(days)만큼 역순으로 리스트를 생성하며,
     * 일기가 없는 날짜는 null로 채웁니다.
     */
    private fun List<DiaryWithRelations>.fillEmptyDiaries(
        baseDate: LocalDate,
    ): List<DiaryWithRelations?> {
        val diaryMap = this.associateBy { it.diary.date }
        return (0 until HEATMAP_SIZE).map { offset ->
            diaryMap[baseDate.minusDays(offset)]
        }
    }
}