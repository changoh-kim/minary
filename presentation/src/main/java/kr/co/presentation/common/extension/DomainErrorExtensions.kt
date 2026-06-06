package kr.co.presentation.common.extension

import kr.co.domain.error.DomainError

/**
 * 에러 콜백을 정의하는 수신 객체 클래스입니다.
 * [unexpected]는 필수 핸들러이며, 정의되지 않은 모든 에러의 Fallback 역할을 수행합니다.
 */
class DomainErrorHandler {
    // 공통 에러
    var networkUnavailable: (suspend () -> Unit)? = null // 네트워크 문제
    var timeout: (suspend () -> Unit)? = null            // 타임아웃

    /**
     * 예기치 않은 에러([DomainError.Unexpected]) 또는 명시적으로 핸들러를 등록하지 않은 모든 에러를 처리합니다.
     * [DomainError.Unexpected] 발생 시 원인이 되는 [Throwable]이 전달되며, 그 외의 에러가 처리되지 않아 호출된 경우 null이 전달됩니다.
     */
    lateinit var unexpected: (suspend (systemError: Throwable?) -> Unit)

    // [Auth 에러]
    var invalidCredentials: (suspend () -> Unit)? = null // 인증 정보 오류
    var authUserNotFound: (suspend () -> Unit)? = null   // 사용자 없음
    var emailAlreadyInUse: (suspend () -> Unit)? = null  // 이메일 중복
    var weakPassword: (suspend () -> Unit)? = null       // 비밀번호 정책 미충족
    var tooManyRequests: (suspend () -> Unit)? = null    // 과도한 요청
    var requiresRecentLogin: (suspend () -> Unit)? = null // 재인증 필요
    var userDisabled: (suspend () -> Unit)? = null       // 계정 비활성화
    var tokenExpired: (suspend () -> Unit)? = null       // 토큰 만료

    // [Store 에러]
    var storePermissionDenied: (suspend () -> Unit)? = null
    var storeNotFound: (suspend () -> Unit)? = null
    var quotaExceeded: (suspend () -> Unit)? = null
    var alreadyExists: (suspend () -> Unit)? = null
    var unauthenticated: (suspend () -> Unit)? = null

    // [Storage 에러]
    var storagePermissionDenied: (suspend () -> Unit)? = null
    var storageQuotaExceeded: (suspend () -> Unit)? = null
    var storageNotFound: (suspend () -> Unit)? = null

    // [Diary 에러]
    var diaryNotFound: (suspend () -> Unit)? = null

    // [Time 에러]
    val timeNotInitialized: (suspend () -> Unit)? = null
}

/**
 * 도메인 에러를 처리하기 위한 DSL 스타일의 확장 함수입니다.
 *
 * 처리하고 싶은 특정 에러들만 핸들러를 등록하고, 나머지는 [DomainErrorHandler.unexpected]에서 공통으로 처리합니다.
 * [DomainErrorHandler.unexpected]는 필수 항목으로, 누락 시 [UninitializedPropertyAccessException]이 발생합니다.
 */
suspend inline fun handleDomainError(
    error: DomainError,
    crossinline builder: DomainErrorHandler.() -> Unit
) {
    val errorHandler = DomainErrorHandler().apply(builder)

    val handler: (suspend () -> Unit)? = when (error) {
        is DomainError.NetworkUnavailable -> errorHandler.networkUnavailable
        is DomainError.Timeout -> errorHandler.timeout

        // Auth
        is DomainError.Auth.InvalidCredentials -> errorHandler.invalidCredentials
        is DomainError.Auth.UserNotFound -> errorHandler.authUserNotFound
        is DomainError.Auth.EmailAlreadyInUse -> errorHandler.emailAlreadyInUse
        is DomainError.Auth.WeakPassword -> errorHandler.weakPassword
        is DomainError.Auth.TooManyRequests -> errorHandler.tooManyRequests
        is DomainError.Auth.RequiresRecentLogin -> errorHandler.requiresRecentLogin
        is DomainError.Auth.UserDisabled -> errorHandler.userDisabled
        is DomainError.Auth.TokenExpired -> errorHandler.tokenExpired

        // Store
        is DomainError.Store.PermissionDenied -> errorHandler.storePermissionDenied
        is DomainError.Store.NotFound -> errorHandler.storeNotFound
        is DomainError.Store.QuotaExceeded -> errorHandler.quotaExceeded
        is DomainError.Store.AlreadyExists -> errorHandler.alreadyExists
        is DomainError.Store.Unauthenticated -> errorHandler.unauthenticated

        // Storage
        is DomainError.Storage.PermissionDenied -> errorHandler.storagePermissionDenied
        is DomainError.Storage.QuotaExceeded -> errorHandler.storageQuotaExceeded
        is DomainError.Storage.NotFound -> errorHandler.storageNotFound

        // Diary
        is DomainError.Diary.NotFound -> errorHandler.diaryNotFound

        // Time
        is DomainError.Time.NotInitialized -> errorHandler.timeNotInitialized

        is DomainError.Unexpected -> null
    }

    if (handler != null) {
        handler.invoke()
    } else {
        val systemError =
            if (error is DomainError.Unexpected)
                error.cause // System 에러 반환
            else
                null        // Domain 에러 반환(개발자가 Domain 에러에 매핑되는 handler를 등록하지 않았을때 발생)

        errorHandler.unexpected(systemError)
    }
}