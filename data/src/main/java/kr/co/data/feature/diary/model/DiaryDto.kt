package kr.co.data.feature.diary.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
data class DiaryDto(
    @SerialName(ID)
    @get:PropertyName(ID)
    @PropertyName(ID)
    val id: String = UUID.randomUUID().toString(),

    @SerialName(DATE)
    @get:PropertyName(DATE)
    @PropertyName(DATE)
    val date: String = "",

    @SerialName(TITLE)
    @get:PropertyName(TITLE)
    @PropertyName(TITLE)
    val title: String = "",

    @SerialName(CONTENT)
    @get:PropertyName(CONTENT)
    @PropertyName(CONTENT)
    val content: String = "",

    @SerialName(EMOTIONS)
    @get:PropertyName(EMOTIONS)
    @PropertyName(EMOTIONS)
    val emotions: List<String> = emptyList(),

    @SerialName(IMAGE_URLS)
    @get:PropertyName(IMAGE_URLS)
    @PropertyName(IMAGE_URLS)
    val imageUrls: List<String> = emptyList(),

    @SerialName(CREATED_AT)
    @get:PropertyName(CREATED_AT)
    @PropertyName(CREATED_AT)
    val createdAt: Long = System.currentTimeMillis(),

    @SerialName(LAST_MODIFIED_AT)
    @get:PropertyName(LAST_MODIFIED_AT)
    @PropertyName(LAST_MODIFIED_AT)
    val lastModifiedAt: Long = System.currentTimeMillis(),
) {
    constructor() : this(
        id = UUID.randomUUID().toString(),
        date = "",
        title = "",
        content = "",
        emotions = emptyList(),
        imageUrls = emptyList(),
        createdAt = System.currentTimeMillis(),
        lastModifiedAt = System.currentTimeMillis(),
    )

    companion object {
        const val ID = "id"
        const val DATE = "date"
        const val TITLE = "title"
        const val CONTENT = "content"
        const val EMOTIONS = "emotions"
        const val IMAGE_URLS = "imageUrls"
        const val CREATED_AT = "createdAt"
        const val LAST_MODIFIED_AT = "lastModifiedAt"
    }
}