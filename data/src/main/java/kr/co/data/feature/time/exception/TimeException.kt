package kr.co.data.feature.time.exception


sealed class TimeException(message: String) : RuntimeException(message) {
    // UTC 시간이 초기화 되지 않음
    class NotInitializedException(message: String = "") : TimeException(message)
}