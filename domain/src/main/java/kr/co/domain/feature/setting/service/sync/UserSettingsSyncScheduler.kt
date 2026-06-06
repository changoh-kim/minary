package kr.co.domain.feature.setting.service.sync

interface UserSettingsSyncScheduler {
    fun scheduleSettingsPush()

    fun cancelSettingsPush()
    fun cancelAll()
}