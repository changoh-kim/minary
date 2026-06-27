package kr.co.domain.testing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DomainTestingInfrastructureTest : DomainCoroutineTest() {

    @Test
    fun `runDomainTest executes with fake server time`() {
        runDomainTest {
            val serverTime = FakeServerTimeProvider(currentTime = 42L)

            assertEquals(42L, serverTime.now())
            serverTime.sync().assertOk(Unit)
            assertEquals(1, serverTime.syncCallCount)
        }
    }
}
