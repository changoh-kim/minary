package kr.co.domain.feature.diary.sync

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError
import java.time.YearMonth

interface DiarySyncManager {
    suspend fun performChunkedSync(userId: String): Result<Boolean, DomainError>
    suspend fun performImmediatePush(userId: String): Result<Unit, DomainError>
    suspend fun performMonthSync(userId: String, yearMonth: YearMonth): Result<Unit, DomainError>
    suspend fun performInitialPull(
        userId: String,
        onProgress: (Float) -> Unit
    ): Result<Unit, DomainError>
}