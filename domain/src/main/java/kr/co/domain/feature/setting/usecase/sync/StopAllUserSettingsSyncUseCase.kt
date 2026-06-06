package kr.co.domain.feature.setting.usecase.sync

import kr.co.domain.feature.setting.service.sync.UserSettingsSyncScheduler
import javax.inject.Inject

class StopAllUserSettingsSyncUseCase @Inject constructor(
    private val userSettingsSyncScheduler: UserSettingsSyncScheduler,
    private val stopRealtimeUserSettingsSync: StopRealtimeUserSettingsSyncUseCase,
) {
    operator fun invoke() {
        userSettingsSyncScheduler.cancelAll()
        stopRealtimeUserSettingsSync()
    }
}