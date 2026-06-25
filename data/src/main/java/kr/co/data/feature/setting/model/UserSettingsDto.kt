package kr.co.data.feature.setting.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kr.co.core.datastore.proto.ThemeProto

@Keep
@Serializable
data class UserSettingsDto(

    @SerialName(THEME)
    @get:PropertyName(THEME)
    @PropertyName(THEME)
    val theme: String = ThemeProto.SYSTEM.name,

    @SerialName(DIARY_SYNC_ENABLED)
    @get:PropertyName(DIARY_SYNC_ENABLED)
    @PropertyName(DIARY_SYNC_ENABLED)
    val diarySyncEnabled: Boolean = false,

    @SerialName(LAST_MODIFIED_AT)
    @get:PropertyName(LAST_MODIFIED_AT)
    @PropertyName(LAST_MODIFIED_AT)
    val lastModifiedAt: Long = System.currentTimeMillis(),
) {
    constructor() : this(
        theme = ThemeProto.SYSTEM.name,
        diarySyncEnabled = false,
        lastModifiedAt = 0L,
    )

    companion object {
        const val THEME = "theme"
        const val DIARY_SYNC_ENABLED = "diarySyncEnabled"
        const val LAST_MODIFIED_AT = "lastModifiedAt"
    }
}