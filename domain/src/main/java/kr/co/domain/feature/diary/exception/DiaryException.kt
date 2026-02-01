package kr.co.domain.feature.diary.exception


/** 다이어리 최상위 예외 */
open class DiaryException(message: String, cause: Throwable? = null) : Exception(message, cause)
/** 다이어리를 찾을 수 없음 */
class DiaryNotFoundException(message: String) : DiaryException(message)