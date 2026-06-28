package kr.co.core.di.module

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.coroutines.ContinuationInterceptor

class CoroutineScopesModuleTest {

    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `provides application scope with requested dispatcher and job`() {
        val scope = CoroutineScopesModule.providesCoroutineScope(dispatcher)

        assertEquals(dispatcher, scope.coroutineContext[ContinuationInterceptor] as CoroutineDispatcher)
        assertNotNull(scope.coroutineContext[Job])

        scope.cancel()
    }

    @Test
    fun `provides io and main scopes with requested dispatchers and jobs`() {
        val ioScope = CoroutineScopesModule.providesIoScope(dispatcher)
        val mainScope = CoroutineScopesModule.providesMainScope(dispatcher)

        assertEquals(dispatcher, ioScope.coroutineContext[ContinuationInterceptor] as CoroutineDispatcher)
        assertEquals(dispatcher, mainScope.coroutineContext[ContinuationInterceptor] as CoroutineDispatcher)
        assertNotNull(ioScope.coroutineContext[Job])
        assertNotNull(mainScope.coroutineContext[Job])

        ioScope.cancel()
        mainScope.cancel()
    }
}
