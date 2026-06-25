package kr.co.domain.feature.setting.sync

import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError

interface UserSettingsSyncManager {
    suspend fun syncSettings(userId: String): Result<Unit, DomainError>
    suspend fun pushSettings(userId: String): Result<Unit, DomainError>
    suspend fun pullSettings(userId: String): Result<Unit, DomainError>
}