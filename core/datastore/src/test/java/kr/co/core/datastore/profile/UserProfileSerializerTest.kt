package kr.co.core.datastore.profile

import androidx.datastore.core.CorruptionException
import kr.co.core.datastore.proto.GenderProto
import kr.co.core.datastore.proto.UserProfileProto
import kr.co.core.datastore.testing.BaseUnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class UserProfileSerializerTest : BaseUnitTest() {

    private val serializer = UserProfileSerializer()

    @Test
    fun `default value is default proto instance`() {
        assertEquals(UserProfileProto.getDefaultInstance(), serializer.defaultValue)
    }

    @Test
    fun `writes and reads profile proto`() {
        runCoreTest {
            val expected = UserProfileProto.newBuilder()
                .setUid("uid-test")
                .setEmail("email-test")
                .setName("name-test")
                .setGender(GenderProto.NONE)
                .setBirthday("2000-01-01")
                .setLastModifiedAt(1_700_000_000_000L)
                .build()
            val output = ByteArrayOutputStream()

            serializer.writeTo(expected, output)
            val actual = serializer.readFrom(ByteArrayInputStream(output.toByteArray()))

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `throws corruption exception for invalid profile proto`() {
        assertThrows(CorruptionException::class.java) {
            runCoreTest {
                serializer.readFrom(ByteArrayInputStream(byteArrayOf(0x80.toByte())))
            }
        }
    }
}
