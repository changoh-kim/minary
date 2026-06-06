package kr.co.domain.feature.profile.service.sync

interface UserProfileSyncScheduler {
    fun scheduleProfilePush()
    fun scheduleProfilePhotoPush()

    fun cancelProfilePush()
    fun cancelProfilePhotoPush()
    fun cancelAll()
}