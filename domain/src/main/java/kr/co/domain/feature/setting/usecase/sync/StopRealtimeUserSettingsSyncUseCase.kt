package kr.co.domain.feature.setting.usecase.sync

import kr.co.domain.feature.setting.service.sync.UserSettingsRealtimeSyncScheduler
import javax.inject.Inject


class StopRealtimeUserSettingsSyncUseCase @Inject constructor(
    private val userSettingsRealtimeSyncScheduler: UserSettingsRealtimeSyncScheduler,
) {
    operator fun invoke(): Unit = userSettingsRealtimeSyncScheduler.stopListening()
}