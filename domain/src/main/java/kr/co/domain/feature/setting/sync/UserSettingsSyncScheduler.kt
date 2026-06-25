package kr.co.domain.feature.setting.sync

interface UserSettingsSyncScheduler {
    fun scheduleSettingsPush()

    fun cancelSettingsPush()
    fun cancelAll()
}