package kr.co.domain.feature.setting.service.sync

import com.github.michaelbull.result.Result
import kr.co.domain.error.DomainError

interface UserSettingsSyncManager {
    suspend fun syncSettings(userId: String): Result<Unit, DomainError>
    suspend fun pushSettings(userId: String): Result<Unit, DomainError>
    suspend fun pullSettings(userId: String): Result<Unit, DomainError>
}