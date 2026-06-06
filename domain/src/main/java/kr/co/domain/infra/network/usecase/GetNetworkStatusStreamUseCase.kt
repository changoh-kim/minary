package kr.co.domain.infra.network.usecase

import kotlinx.coroutines.flow.Flow
import kr.co.domain.infra.network.service.NetworkMonitor
import javax.inject.Inject

class GetNetworkStatusStreamUseCase @Inject constructor(
    private val networkMonitor: NetworkMonitor // Domain에 위치한 인터페이스
) {
    operator fun invoke(): Flow<Boolean> = networkMonitor.isOnline
}