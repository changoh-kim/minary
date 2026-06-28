package kr.co.core.di.module

import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class DispatchersModuleTest {

    @Test
    fun `provides platform dispatchers`() {
        assertSame(Dispatchers.Default, DispatchersModule.providesDefaultDispatcher())
        assertSame(Dispatchers.IO, DispatchersModule.providesIoDispatcher())
        assertSame(Dispatchers.Main, DispatchersModule.providesMainDispatcher())
    }
}
