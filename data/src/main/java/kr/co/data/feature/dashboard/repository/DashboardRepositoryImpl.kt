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
import kr.co.domain.feature.emotion.model.Emotion
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val databaseProvider: UserDatabaseProvider,
) : DashboardRepository {
    companion object {
        const val HEATMAP_SIZE = 40L
    }

    private val diaryDao get() = databaseProvider.getDatabase().diaryDao()
    override suspend fun getDashboard(): Result<Dashboard, DomainError> =
        runSuspendCatching {
            val startDate = LocalDate.now()
            val endDate = startDate.minusDays(HEATMAP_SIZE)

            val recentDiaries =
                diaryDao.getDiariesByDateRangeWithRelations(
                    startDate = startDate,
                    endDate = endDate
                ).fillEmptyDiaries(startDate)
            val totalDiaryCount = diaryDao.getTotalDiaryCount()
            val totalWordCount = diaryDao.getTotalWordCount()
            val mostFrequentEmotion =
                diaryDao.getMostFrequentEmotionStats()?.emotion ?: Emotion.UNKNOWN
            val leastFrequentEmotion =
                diaryDao.getLeastFrequentEmotionStaus()?.emotion ?: Emotion.UNKNOWN

            DashboardModel(
                recentDiaries = recentDiaries,
                totalDiaryCount = totalDiaryCount,
                totalWordCount = totalWordCount,
                mostFrequentEmotion = mostFrequentEmotion,
                leastFrequentEmotion = leastFrequentEmotion,
            ).toDashboard()
        }
        .onErr { Log.e(TAG, "Failed to get dashboard", it) }
        .mapError { it.toDomainError() }

    /**
     * 기준일(today)로부터 지정된 일수(days)만큼 역순으로 리스트를 생성하며,
     * 일기가 없는 날짜는 null로 채웁니다.
     */
    private fun List<DiaryWithRelations>.fillEmptyDiaries(
        baseDate: LocalDate,
    ): List<DiaryWithRelations?> {
        val diaryMap = this.associateBy { it.diary.date }
        return (0..HEATMAP_SIZE).map { offset ->
            diaryMap[baseDate.minusDays(offset)]
        }
    }
}