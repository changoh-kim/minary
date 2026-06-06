package kr.co.domain.common.extension

import kr.co.domain.feature.diary.model.DiarySyncStatus
import kr.co.domain.feature.emotion.model.Emotion
import kr.co.domain.feature.profile.model.Gender
import kr.co.domain.feature.setting.model.AppTheme
import java.time.LocalDate
import java.time.format.DateTimeParseException


/**
 * ISO-8601 형식 문자열("2023-10-25")을 LocalDate로 안전하게 변환합니다.
 * 형식이 올바르지 않거나 입력이 null이면 null을 반환합니다.
 */
fun String?.toLocalDate(): LocalDate? {
    if (this.isNullOrBlank()) return null
    return try {
        LocalDate.parse(this)
    } catch (e: DateTimeParseException) {
        e.printStackTrace()
        null
    }
}

/**
 * Epoch Day(Long)를 LocalDate로 안전하게 변환합니다.
 * 유효하지 않은 값이거나 입력이 null이면 null을 반환합니다.
 */
fun Long?.toLocalDate(): LocalDate? {
    if (this == null) return null
    return try {
        LocalDate.ofEpochDay(this)
    } catch (e: Exception) {
        e.printStackTrace()
        // LocalDate.ofEpochDay는 지원 범위를 벗어나는 Long 값에 대해 DateTimeException을 발생시킬 수 있습니다.
        null
    }
}

/**
 * 문자열을 Emotion Enum으로 변환합니다.
 * - 대소문자를 무시합니다.
 * - 일치하는 값이 없으면 UNKNOWN을 반환합니다.
 */
fun String.toEmotion(): Emotion {
    val normalized = this.trim()
    return Emotion.entries.find {
        it.name.equals(normalized, ignoreCase = true)
    } ?: Emotion.UNKNOWN
}

/**
 * 문자열을 Gender Enum으로 변환합니다.
 * - 대소문자를 무시합니다.
 * - 일치하는 값이 없으면 NONE을 반환합니다.
 */
fun String.toGender(): Gender {
    val normalized = this.trim()
    return Gender.entries.find {
        it.name.equals(normalized, ignoreCase = true)
    } ?: Gender.NONE
}

/**
 * 문자열을 AppTheme Enum으로 변환합니다.
 * - 대소문자를 무시합니다.
 * - 일치하는 값이 없으면 SYSTEM을 반환합니다.
 */
fun String.toAppTheme(): AppTheme {
    val normalized = this.trim()
    return AppTheme.entries.find {
        it.name.equals(normalized, ignoreCase = true)
    } ?: AppTheme.SYSTEM
}

/**
 * 문자열을 DiarySyncStatus Enum으로 변환합니다.
 * - 대소문자를 무시합니다.
 * - 일치하는 값이 없으면 LOCAL_ONLY 반환합니다.
 */
fun String.toSyncStatus(): DiarySyncStatus {
    val normalized = this.trim()
    return DiarySyncStatus.entries.find {
        it.name.equals(normalized, ignoreCase = true)
    } ?: DiarySyncStatus.PENDING_CREATE
}

/**
 * Boolean 값을 Int로 변환합니다. (True -> 1, False -> 0)
 * - SQLite 등 Boolean 타입이 없는 DB에 저장할 때 유용합니다.
 */
fun Boolean.toInt(): Int = if (this) 1 else 0

/**
 * Int 값을 Boolean으로 변환합니다. (1 -> True, 그 외 -> False)
 */
fun Int.toBoolean(): Boolean = this == 1