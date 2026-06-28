package kr.co.core.ui.common.load

import kr.co.core.common.error.DomainError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LoadStateTest {

    @Test
    fun `load state exposes typed status flags and data`() {
        val success = LoadState.Success("value-test")
        val error = LoadState.Error(DomainError.Timeout)

        assertTrue(LoadState.Uninitialized.isUninitialized)
        assertTrue(LoadState.Loading.isLoading)
        assertTrue(success.isSuccess)
        assertEquals("value-test", success.data)
        assertTrue(error.isError)
        assertEquals(DomainError.Timeout, error.error)
        assertFalse(success.isLoading)
        assertNull(error.data)
    }
}
