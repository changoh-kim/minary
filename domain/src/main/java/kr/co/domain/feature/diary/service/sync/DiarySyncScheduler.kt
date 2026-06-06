package kr.co.domain.feature.diary.service.sync

interface DiarySyncScheduler {
    fun scheduleFullSync()
    fun rescheduleFullSync()
    fun scheduleImmediateSync()
    fun schedulePeriodicSync()

    fun cancelFullSync()
    fun cancelImmediateSync()
    fun cancelPeriodicSync()
    fun cancelAllSync()
}