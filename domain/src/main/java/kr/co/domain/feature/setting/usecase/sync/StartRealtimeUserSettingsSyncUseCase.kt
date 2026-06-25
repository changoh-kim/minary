package kr.co.domain.feature.setting.usecase.sync

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kr.co.core.common.error.DomainError
import kr.co.domain.feature.setting.sync.UserSettingsRealtimeSyncScheduler
import javax.inject.Inject


class StartRealtimeUserSettingsSyncUseCase @Inject constructor(
    private val userSettingsRealtimeSyncScheduler: UserSettingsRealtimeSyncScheduler,
) {
    suspend operator fun invoke(): Result<Unit, DomainError> {
        userSettingsRealtimeSyncScheduler.startListening()
        return Ok(Unit)
    }
}