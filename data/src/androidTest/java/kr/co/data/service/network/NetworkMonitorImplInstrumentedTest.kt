package kr.co.data.service.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kr.co.data.testing.BaseDataInstrumentationTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkMonitorImplInstrumentedTest : BaseDataInstrumentationTest() {
    @Test
    fun isOnline_emits_false_when_active_network_is_missing() = runDataAndroidTest {
        val fixture = NetworkMonitorFixture(activeNetworkConnected = false)

        val actual = fixture.monitor.isOnline.first()

        assertEquals(false, actual)
        assertTrue(fixture.registeredRequest.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        verify(exactly = 1) {
            fixture.connectivityManager.unregisterNetworkCallback(fixture.registeredCallback)
        }
    }

    @Test
    fun isOnline_emits_true_when_active_network_has_internet_capability() = runDataAndroidTest {
        val fixture = NetworkMonitorFixture(activeNetworkConnected = true)

        val actual = fixture.monitor.isOnline.first()

        assertEquals(true, actual)
        assertTrue(fixture.registeredRequest.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        verify(exactly = 1) {
            fixture.connectivityManager.unregisterNetworkCallback(fixture.registeredCallback)
        }
    }

    @Test
    fun isOnline_emits_distinct_callback_changes_after_initial_state() = runDataAndroidTest {
        val fixture = NetworkMonitorFixture(activeNetworkConnected = false)

        val values = async {
            fixture.monitor.isOnline.take(3).toList()
        }
        advanceUntilIdle()

        fixture.registeredCallback.onAvailable(mockk())
        fixture.registeredCallback.onAvailable(mockk())
        fixture.registeredCallback.onLost(mockk())
        advanceUntilIdle()

        assertEquals(listOf(false, true, false), values.await())
        verify(exactly = 1) {
            fixture.connectivityManager.unregisterNetworkCallback(fixture.registeredCallback)
        }
    }

    private inner class NetworkMonitorFixture(
        activeNetworkConnected: Boolean,
    ) {
        private val network = mockk<Network>()
        private val capabilities = mockk<NetworkCapabilities> {
            every { hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        }
        val connectivityManager = mockk<ConnectivityManager>()
        private val requestSlot = slot<NetworkRequest>()
        private val callbackSlot = slot<ConnectivityManager.NetworkCallback>()

        val monitor: NetworkMonitorImpl

        val registeredRequest: NetworkRequest
            get() = requestSlot.captured

        val registeredCallback: ConnectivityManager.NetworkCallback
            get() = callbackSlot.captured

        init {
            every { connectivityManager.activeNetwork } returns
                if (activeNetworkConnected) network else null
            every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
            every {
                connectivityManager.registerNetworkCallback(
                    capture(requestSlot),
                    capture(callbackSlot),
                )
            } just Runs
            every { connectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>()) } just Runs

            val context = mockk<Context> {
                every { getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
            }
            monitor = NetworkMonitorImpl(context)
        }
    }
}
