package kr.co.data.feature.dashboard.repository

import android.util.Log
import kr.co.data.local.dao.DiaryDao
import kr.co.domain.common.extension.toEmotion
import kr.co.domain.feature.dashboard.model.Dashboard
import kr.co.domain.feature.dashboard.repository.DashboardRepository
import kr.co.domain.feature.emotion.Emotion
import javax.inject.Inject


class DashboardRepositoryImpl @Inject constructor(
    private val diaryDao: DiaryDao,
) : DashboardRepository {

    companion object {
        private val TAG: String = DashboardRepositoryImpl::class.java.simpleName
    }

    override suspend fun getDashboard(): Result<Dashboard> {
        return runCatching {
            val recentEmotions = diaryDao.getDashboardRecentEmotions(30)
            val countStats = diaryDao.getDashboardCountStats()
            val emotionStats = diaryDao.getDashboardEmotionStats()

            Dashboard(
                recentEmotions = recentEmotions.map { it.toEmotion() },
                totalDiaries = countStats?.totalDiaries ?: 0,
                totalWords = countStats?.totalWords ?: 0,
                dominantEmotion = emotionStats.firstOrNull()?.emotion?.toEmotion() ?: Emotion.UNKNOWN,
                rarestEmotion = emotionStats.lastOrNull()?.emotion?.toEmotion() ?: Emotion.UNKNOWN
            )
        }.onFailure {
            Log.e(TAG, "getDashboard failed: ${it.message}", it)
        }
    }
}