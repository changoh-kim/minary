package kr.co.domain.infra.remote.usecase

import kr.co.domain.infra.remote.model.ServiceStatus
import kr.co.domain.infra.remote.repository.RemoteConfigRepository
import javax.inject.Inject

class CheckServiceStatusUseCase @Inject constructor(
    private val configRepository: RemoteConfigRepository,
) {
    suspend operator fun invoke(): ServiceStatus {
        configRepository.fetchAndActivate()

        return if (configRepository.isMaintenanceMode()) {
            ServiceStatus.Maintenance(configRepository.getMaintenanceReason())
        } else {
            ServiceStatus.Active
        }
    }
}