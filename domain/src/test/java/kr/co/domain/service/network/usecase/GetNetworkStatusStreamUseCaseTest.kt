package kr.co.domain.service.network.usecase

import kr.co.domain.testing.fake.FakeNetworkMonitor
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GetNetworkStatusStreamUseCaseTest {

    @Test
    fun `returns network status flow`() {
        val networkMonitor = FakeNetworkMonitor(initialOnline = true)
        val useCase = GetNetworkStatusStreamUseCase(networkMonitor)

        val result = useCase()

        assertSame(networkMonitor.isOnline, result)
    }
}
