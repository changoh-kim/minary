package kr.co.domain.exception


/** 다이어리 관련 비즈니스 규칙을 위반했을 때 발생하는 최상위 예외 */
open class DiaryException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** 요청한 다이어리를 찾을 수 없을 때 발생하는 예외 */
class DiaryNotFoundException(message: String) : DiaryException(message)
