package kr.co.core.common.state

import kr.co.core.common.error.DomainError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class SyncProcessStateTest {

    @Test
    fun `sync process state keeps progress and domain error values`() {
        assertEquals(0.5f, SyncProcessState.InProgress.Determinate(0.5f).progress)
        assertSame(SyncProcessState.InProgress.Indeterminate, SyncProcessState.InProgress.Indeterminate)
        assertSame(SyncProcessState.Completed, SyncProcessState.Completed)
        assertEquals(
            SyncProcessState.Failed(DomainError.Timeout),
            SyncProcessState.Failed(DomainError.Timeout),
        )
    }
}
