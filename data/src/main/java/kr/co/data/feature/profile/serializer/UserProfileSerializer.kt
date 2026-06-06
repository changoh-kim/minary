package kr.co.data.feature.profile.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import kr.co.data.proto.UserProfileProto
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileSerializer @Inject constructor() : Serializer<UserProfileProto> {

    override val defaultValue: UserProfileProto = UserProfileProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserProfileProto {
        try {
            return UserProfileProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read UserProfileProto.", exception)
        }
    }

    override suspend fun writeTo(t: UserProfileProto, output: OutputStream) {
        t.writeTo(output)
    }
}