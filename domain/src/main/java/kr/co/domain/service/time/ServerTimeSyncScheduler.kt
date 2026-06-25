package kr.co.domain.service.time


interface ServerTimeSyncScheduler {
    fun scheduleSync()
    fun cancelSync()
}