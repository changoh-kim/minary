package kr.co.domain.infra.remote.model

sealed class ServiceStatus {
    object Active : ServiceStatus()
    data class Maintenance(val reason: String) : ServiceStatus()
}