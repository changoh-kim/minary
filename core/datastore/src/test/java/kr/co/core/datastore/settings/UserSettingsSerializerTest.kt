package kr.co.core.datastore.settings

import androidx.datastore.core.CorruptionException
import kr.co.core.datastore.proto.ThemeProto
import kr.co.core.datastore.proto.UserSettingsProto
import kr.co.core.datastore.testing.BaseUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class UserSettingsSerializerTest : BaseUnitTest() {

    private val serializer = UserSettingsSerializer()

    @Test
    fun `default value is default proto instance`() {
        assertEquals(UserSettingsProto.getDefaultInstance(), serializer.defaultValue)
    }

    @Test
    fun `writes and reads settings proto`() {
        runCoreTest {
            val expected = UserSettingsProto.newBuilder()
                .setTheme(ThemeProto.DARK)
                .setDiarySyncEnabled(true)
                .setLastModifiedAt(1_700_000_000_000L)
                .build()
            val output = ByteArrayOutputStream()

            serializer.writeTo(expected, output)
            val actual = serializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `throws corruption exception for invalid settings proto`() {
        assertThrows(CorruptionException::class.java) {
            runCoreTest {
                serializer.readFrom(ByteArrayInputStream(byteArrayOf(0x80.toByte())))
            }
        }
    }
}
