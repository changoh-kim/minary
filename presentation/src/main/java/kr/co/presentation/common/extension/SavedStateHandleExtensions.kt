package kr.co.presentation.common.extension

import androidx.lifecycle.SavedStateHandle
import java.time.LocalDate


/**
 * SavedStateHandle에서 Key에 해당하는 값을 안전하게 Long 타입으로 반환합니다.
 * 내부적으로 Integer로 저장되었더라도 Long으로 변환해줍니다.
 * 값이 없거나 타입이 맞지 않으면 null을 반환합니다.
 */
fun SavedStateHandle.getLong(key: String): Long? {
    return (this[key] as? Number)?.toLong()
}

/**
 * SavedStateHandle에서 Key에 해당하는 값을 안전하게 Long 타입으로 반환합니다.
 * 내부적으로 Integer로 저장되었더라도 Long으로 변환해줍니다.
 * 값이 없거나 타입이 맞지 않으면 defaultValue를 반환합니다.
 */
fun SavedStateHandle.getLong(key: String, defaultValue: Long): Long {
    return (this[key] as? Number)?.toLong() ?: defaultValue
}

/**
 * SavedStateHandle에서 Key에 해당하는 epochDay 값을 안전하게 LocalDate 타입으로 반환합니다.
 * 값이 없거나 타입이 맞지 않으면 null을 반환합니다.
 */
fun SavedStateHandle.getLocalDate(key: String): LocalDate? {
    val epochDay = (this[key] as? Number)?.toLong()
    return if (epochDay != null) {
        LocalDate.ofEpochDay(epochDay)
    } else {
        null
    }
}

/**
 * SavedStateHandle에서 Key에 해당하는 epochDay 값을 안전하게 LocalDate 타입으로 반환합니다.
 * 값이 없거나 타입이 맞지 않으면 defaultValue를 반환합니다.
 */
fun SavedStateHandle.getLocalDate(key: String, defaultValue: LocalDate): LocalDate {
    val epochDay = (this[key] as? Number)?.toLong()
    return if (epochDay != null) {
        LocalDate.ofEpochDay(epochDay)
    } else {
        defaultValue
    }
}