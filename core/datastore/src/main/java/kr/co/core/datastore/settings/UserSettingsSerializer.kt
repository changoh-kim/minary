package kr.co.core.datastore.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import kr.co.core.datastore.proto.UserSettingsProto
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsSerializer @Inject constructor() : Serializer<UserSettingsProto> {

    override val defaultValue: UserSettingsProto = UserSettingsProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserSettingsProto {
        try {
            return UserSettingsProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read UserSettingsProto.", exception)
        }
    }

    override suspend fun writeTo(t: UserSettingsProto, output: OutputStream) {
        t.writeTo(output)
    }
}