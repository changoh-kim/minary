package kr.co.data.feature.account.exception

sealed class AccountException : Exception() {
    class UserNotFoundException : AccountException()
    class SessionExpiredException : AccountException()
}
