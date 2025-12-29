package kr.co.domain.exception


sealed class AuthException : Throwable() {
    object CreateUserIsNullException : AuthException()
    object SignInUserIsNullException : AuthException()
    object CurrentUserIsNullException : AuthException()
    object UnknownException : AuthException()
}