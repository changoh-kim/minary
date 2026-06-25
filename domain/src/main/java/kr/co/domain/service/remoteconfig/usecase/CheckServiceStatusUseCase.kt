package kr.co.domain.service.remoteconfig.usecase

import kr.co.domain.service.remoteconfig.model.ServiceStatus
import kr.co.domain.service.remoteconfig.repository.RemoteConfigRepository
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