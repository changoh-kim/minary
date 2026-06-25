package kr.co.domain.feature.profile.sync

interface UserProfileSyncScheduler {
    fun scheduleProfilePush()
    fun scheduleProfilePhotoPush()

    fun cancelProfilePush()
    fun cancelProfilePhotoPush()
    fun cancelAll()
}