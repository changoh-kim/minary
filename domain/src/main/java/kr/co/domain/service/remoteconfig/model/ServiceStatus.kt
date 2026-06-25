package kr.co.domain.service.remoteconfig.model

sealed class ServiceStatus {
    object Active : ServiceStatus()
    data class Maintenance(val reason: String) : ServiceStatus()
}