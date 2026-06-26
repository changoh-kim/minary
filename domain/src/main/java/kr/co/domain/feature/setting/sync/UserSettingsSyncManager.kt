package kr.co.domain.feature.setting.sync

import kr.co.core.common.result.AppResult

interface UserSettingsSyncManager {
    suspend fun syncSettings(userId: String): AppResult<Unit>
    suspend fun pushSettings(userId: String): AppResult<Unit>
    suspend fun pullSettings(userId: String): AppResult<Unit>
}