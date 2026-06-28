package kr.co.core.firebase.provider

import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class FirebaseRemoteConfigProviderTest {

    private val remoteConfig = mockk<FirebaseRemoteConfig>()
    private val provider = FirebaseRemoteConfigProvider(remoteConfig)

    @Test
    fun `delegates fetch and value lookups`() {
        val task = mockk<Task<Boolean>>()
        every { remoteConfig.fetchAndActivate() } returns task
        every { remoteConfig.getBoolean("enabled-test") } returns false
        every { remoteConfig.getString("value-test") } returns "config-value-test"
        every { remoteConfig.getLong("timestamp-test") } returns 100L

        assertSame(task, provider.fetchAndActivate())
        assertFalse(provider.getBoolean("enabled-test"))
        assertEquals("config-value-test", provider.getString("value-test"))
        assertEquals(100L, provider.getLong("timestamp-test"))

        verify(exactly = 1) { remoteConfig.fetchAndActivate() }
        verify(exactly = 1) { remoteConfig.getBoolean("enabled-test") }
        verify(exactly = 1) { remoteConfig.getString("value-test") }
        verify(exactly = 1) { remoteConfig.getLong("timestamp-test") }
    }
}
