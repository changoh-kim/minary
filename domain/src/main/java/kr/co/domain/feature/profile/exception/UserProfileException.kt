package kr.co.domain.feature.profile.exception


open class UserProfileException(message: String, cause: Throwable? = null) : Exception(message, cause)

class RemoteUserProfileNotFoundException(message: String) : UserProfileException(message)

class UserProfilePermissionException(message: String, cause: Throwable? = null) : UserProfileException(message, cause)

class UserProfileNetworkException(message: String, cause: Throwable? = null) : UserProfileException(message, cause)

class UserProfileUnknownException(message: String, cause: Throwable? = null) : UserProfileException(message, cause)

class UserProfileNotSavedException(message: String, cause: Throwable? = null) : UserProfileException(message, cause)
