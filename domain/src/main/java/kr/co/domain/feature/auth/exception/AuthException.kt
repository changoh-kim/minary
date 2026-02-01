package kr.co.domain.feature.auth.exception


sealed class AuthException : Throwable() {
    object CreateUserIsNullException : AuthException()
    object SignInUserIsNullException : AuthException()
    object CurrentUserIsNullException : AuthException()
    object UnknownException : AuthException()
}