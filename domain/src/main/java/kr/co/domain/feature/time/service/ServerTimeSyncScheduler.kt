package kr.co.domain.feature.time.service


interface ServerTimeSyncScheduler {
    fun scheduleSync()
    fun cancelSync()
}