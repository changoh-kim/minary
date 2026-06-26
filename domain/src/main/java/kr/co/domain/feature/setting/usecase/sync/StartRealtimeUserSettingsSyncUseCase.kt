package kr.co.domain.feature.setting.usecase.sync

import com.github.michaelbull.result.Ok
import kr.co.core.common.result.AppResult
import kr.co.domain.feature.setting.sync.UserSettingsRealtimeSyncScheduler
import javax.inject.Inject


class StartRealtimeUserSettingsSyncUseCase @Inject constructor(
    private val userSettingsRealtimeSyncScheduler: UserSettingsRealtimeSyncScheduler,
) {
    suspend operator fun invoke(): AppResult<Unit> {
        userSettingsRealtimeSyncScheduler.startListening()
        return Ok(Unit)
    }
}