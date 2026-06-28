package kr.co.core.common.result

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import kr.co.core.common.error.DomainError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AppResultContractTest {

    @Test
    fun `app result exposes domain error as public failure contract`() {
        val success: AppResult<String> = Ok("value-test")
        val failure: AppResult<String> = Err(DomainError.NetworkUnavailable)

        assertEquals("value-test", success.get())
        assertNull(success.getError())
        assertEquals(DomainError.NetworkUnavailable, failure.getError())
        assertNull(failure.get())
    }
}
