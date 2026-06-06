package kr.co.domain.feature.setting.exception


open class UserSettingsException(message: String, cause: Throwable? = null) : Exception(message, cause)

class RemoteUserSettingsNotFoundException(message: String) : UserSettingsException(message)

class UserSettingsPermissionException(message: String, cause: Throwable? = null) : UserSettingsException(message, cause)

class UserSettingsNetworkException(message: String, cause: Throwable? = null) : UserSettingsException(message, cause)

class UserSettingsUnknownException(message: String, cause: Throwable? = null) : UserSettingsException(message, cause)

class UserSettingsNotSavedException(message: String, cause: Throwable? = null) : UserSettingsException(message, cause)