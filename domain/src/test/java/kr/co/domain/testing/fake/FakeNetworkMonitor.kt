package kr.co.domain.testing.fake

import kotlinx.coroutines.flow.MutableStateFlow
import kr.co.domain.service.network.NetworkMonitor

class FakeNetworkMonitor(
    initialOnline: Boolean = false,
) : NetworkMonitor {
    override val isOnline = MutableStateFlow(initialOnline)
}
