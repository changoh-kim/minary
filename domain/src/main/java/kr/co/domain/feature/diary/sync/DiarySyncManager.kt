package kr.co.domain.feature.diary.sync

import kr.co.core.common.result.AppResult
import java.time.YearMonth

interface DiarySyncManager {
    suspend fun performChunkedSync(userId: String): AppResult<Boolean>
    suspend fun performImmediatePush(userId: String): AppResult<Unit>
    suspend fun performMonthSync(userId: String, yearMonth: YearMonth): AppResult<Unit>
    suspend fun performInitialPull(
        userId: String,
        onProgress: (Float) -> Unit
    ): AppResult<Unit>
}