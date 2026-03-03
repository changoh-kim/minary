package kr.co.domain.feature.dashboard.exception


/** Dashboard 최상위 예외 */
open class DashboardException(message: String, cause: Throwable? = null) : Exception(message, cause)
/** Dashboard를 찾을 수 없음 */
class DashboardNotFoundException(message: String) : DashboardException(message)