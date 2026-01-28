package kr.co.data.model.diary

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kr.co.data.local.DiaryTable.COLUMN_CONTENT
import kr.co.data.local.DiaryTable.COLUMN_DATE
import kr.co.data.local.DiaryTable.COLUMN_EMOTION_NAME
import kr.co.data.local.DiaryTable.COLUMN_ID
import kr.co.data.local.DiaryTable.COLUMN_TIMESTAMP
import kr.co.data.local.DiaryTable.COLUMN_TITLE


/**
 * firestore에 저장할 DiaryDto
 *
 * firestore는 @SerialName를 직접 사용하지 않습니다.
 * 다만 나중에 다른 REST API(Gson)의 DTO로 사용할 수 있도록 @SerialName을 사용합니다.
 *
 * @property id
 * @property date
 * @property title
 * @property content
 * @property emotionName
 * @property timestamp
 */
@Keep
@Serializable
data class FirestoreDiaryDto(
    @SerialName(COLUMN_ID)
    val id: Long = 0L,

    @SerialName(COLUMN_DATE)
    val date: String = "",

    @SerialName(COLUMN_TITLE)
    val title: String = "",

    @SerialName(COLUMN_CONTENT)
    val content: String = "",

    @SerialName(COLUMN_EMOTION_NAME)
    val emotionName: String = "",

    @SerialName(COLUMN_TIMESTAMP)
    val timestamp: Long = 0L,
) {
    // firestore의 toObject()는 인자 없는 클래스의 기본 생성자 FirestoreDiaryDto()를 호출합니다.
    // 1. data class로 써 모든 프로퍼티를 초기화 하여 인자 없는 기본 생성자를 자동 호출 가능하게 하거나
    // 2. 또는 인자 없는 부생성자 선언하고, 기본 생성자를 명시적으로 초기화 할 수 있도록 해야합니다.
    constructor() : this(0L, "", "", "", "", 0L)
}