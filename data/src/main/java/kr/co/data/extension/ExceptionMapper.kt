package kr.co.data.extension

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.storage.StorageException
import kr.co.core.common.error.DomainError
import kr.co.data.feature.account.exception.AccountException

fun Throwable.toDomainError(): DomainError = when (this) {
    /**
     * [FirebaseNetworkException]
     * 발생 상황: 기기의 네트워크 연결이 끊겨 있거나, 방화벽 등으로 인해 Firebase 서버에 도달할 수 없을 때 발생합니다.
     * 특징: 주로 클라이언트 측의 인터넷 연결 문제인 경우가 많습니다.
     */
    is FirebaseNetworkException -> DomainError.NetworkUnavailable

    /**
     * [FirebaseTooManyRequestsException] (FirebaseException 상속)
     * 발생 상황:
     * 1. Auth: 특정 IP나 계정에서 단기간에 너무 많은 로그인 시도(무차별 대입 공격), SMS 인증 요청, 이메일 발송 요청을 보낼 때 발생.
     * 2. 공통: Firebase 인프라 측에서 정한 초당 요청 수 제한을 초과했을 때 발생.
     * 대응: 사용자에게 "잠시 후 다시 시도해 주세요"라는 메시지를 보여주는 것이 적절합니다.
     */
    is FirebaseTooManyRequestsException -> DomainError.Auth.TooManyRequests

    /**
     * [AccountException] (커스텀 예외)
     * AccountRemoteDataSource에서 발생하는 인증 관련 예외를 도메인 에러로 매핑합니다.
     */
    is AccountException -> when (this) {
        is AccountException.UserNotFoundException -> DomainError.Auth.UserNotFound
        is AccountException.SessionExpiredException -> DomainError.Auth.TokenExpired
    }

    is FirebaseAuthException -> when (this) {
        /**
         * [FirebaseAuthInvalidCredentialsException]
         * 발생 상황: 제공된 인증 정보(이메일, 비밀번호, 인증 코드 등)가 잘못되었거나 형식에 맞지 않을 때 발생합니다.
         * 하위 클래스: FirebaseAuthWeakPasswordException (비밀번호 취약)이 포함됩니다.
         */
        is FirebaseAuthInvalidCredentialsException -> when (this) {
            /**
             * [FirebaseAuthWeakPasswordException]
             * 발생 상황: 회원가입 시 입력한 비밀번호가 Firebase Auth의 최소 보안 기준(기본 6자 이상)을 충족하지 못할 때 발생합니다.
             */
            is FirebaseAuthWeakPasswordException -> DomainError.Auth.WeakPassword

            /**
             * 이메일 로그인 시 비밀번호가 틀렸거나, 이메일 형식이 유효하지 않은 경우(malformed) 발생합니다.
             */
            else -> DomainError.Auth.InvalidCredentials
        }

        /**
         * [FirebaseAuthUserCollisionException]
         * 발생 상황: 이미 가입된 이메일 주소로 회원가입을 시도하거나, 이미 연결된 인증 수단을 중복으로 연결하려 할 때 발생합니다.
         */
        is FirebaseAuthUserCollisionException -> DomainError.Auth.EmailAlreadyInUse

        /**
         * [FirebaseAuthRecentLoginRequiredException]
         * 발생 상황: 이메일 변경, 비밀번호 변경, 계정 삭제와 같은 '보안 민감 작업'은 마지막 로그인 이후 시간이 오래 지나면 차단됩니다.
         * 대응: 사용자에게 재로그인(Re-authenticate) 프로세스를 거치게 해야 합니다.
         */
        is FirebaseAuthRecentLoginRequiredException -> DomainError.Auth.RequiresRecentLogin

        /**
         * [FirebaseAuthInvalidUserException]
         * 발생 상황: 사용자의 계정 상태 자체가 유효하지 않을 때 발생합니다.
         */
        is FirebaseAuthInvalidUserException -> when (errorCode) {
            // 해당 이메일로 가입된 사용자를 찾을 수 없는 경우 (주로 로그인 시)
            "ERROR_USER_NOT_FOUND" -> DomainError.Auth.UserNotFound
            // 관리자 콘솔에서 계정이 '비활성화(Disabled)' 처리된 경우
            "ERROR_USER_DISABLED" -> DomainError.Auth.UserDisabled
            // 인증 토큰이 만료되어 세션이 끊긴 경우 (다시 로그인 필요)
            "ERROR_USER_TOKEN_EXPIRED" -> DomainError.Auth.TokenExpired
            else -> DomainError.Unexpected
        }

        else -> DomainError.Unexpected
    }

    /**
     * [FirebaseFunctionsException]
     * 발생 상황: Firebase Cloud Functions 호출 중 서버 측에서 에러가 발생하거나, 요청이 거부되었을 때 발생합니다.
     * 특징: gRPC 에러 코드를 사용하여 에러 유형을 구분합니다.
     */
    is FirebaseFunctionsException -> when (code) {
        /**
         * [FirebaseFunctionsException.Code.ALREADY_EXISTS]
         * 발생 상황: 클라이언트가 생성하려고 시도한 리소스(예: 특정 이메일의 계정)가 이미 존재할 때 발생합니다.
         */
        FirebaseFunctionsException.Code.ALREADY_EXISTS -> DomainError.Auth.EmailAlreadyInUse

        /**
         * [FirebaseFunctionsException.Code.INVALID_ARGUMENT]
         * 발생 상황: 클라이언트가 잘못된 인자를 지정했을 때 발생합니다. (예: 잘못된 이메일 형식, 필수 파라미터 누락 등)
         */
        FirebaseFunctionsException.Code.INVALID_ARGUMENT -> DomainError.Auth.InvalidCredentials

        /**
         * [FirebaseFunctionsException.Code.PERMISSION_DENIED]
         * 발생 상황: 호출자에게 지정된 작업을 실행할 권한이 없을 때 발생합니다. (예: 관리자 전용 함수 호출 시도)
         */
        FirebaseFunctionsException.Code.PERMISSION_DENIED -> DomainError.Store.PermissionDenied

        /**
         * [FirebaseFunctionsException.Code.UNAUTHENTICATED]
         * 발생 상황: 요청에 작업에 대한 유효한 인증 자격 증명이 없을 때 발생합니다. (예: 로그인하지 않은 상태에서 인증이 필요한 함수 호출)
         */
        FirebaseFunctionsException.Code.UNAUTHENTICATED -> DomainError.Store.Unauthenticated

        /**
         * [FirebaseFunctionsException.Code.NOT_FOUND]
         * 발생 상황: 요청된 리소스를 찾을 수 없을 때 발생합니다.
         */
        FirebaseFunctionsException.Code.NOT_FOUND -> DomainError.Store.NotFound

        /**
         * [FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED]
         * 발생 상황: 할당량이 부족하거나 전체 시스템의 일시적인 리소스 부족 시 발생합니다.
         */
        FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED -> DomainError.Store.QuotaExceeded

        /**
         * [FirebaseFunctionsException.Code.UNAVAILABLE]
         * 발생 상황: 서비스가 현재 사용 불가능한 상태일 때 발생합니다. 주로 일시적인 현상이며 재시도 시 해결될 수 있습니다.
         */
        FirebaseFunctionsException.Code.UNAVAILABLE -> DomainError.NetworkUnavailable

        /**
         * [FirebaseFunctionsException.Code.DEADLINE_EXCEEDED]
         * 발생 상황: 작업이 완료되기 전에 마감 시간(Timeout)이 초과되었을 때 발생합니다.
         */
        FirebaseFunctionsException.Code.DEADLINE_EXCEEDED -> DomainError.Timeout

        else -> DomainError.Unexpected
    }

    is FirebaseFirestoreException -> when (code) {
        /**
         * [Code.UNAVAILABLE]
         * 발생 상황: Firestore 서버가 일시적으로 다운되었거나, 클라이언트가 오프라인 상태에서 즉각적인 서버 응답을 요구하는 작업을 할 때 발생합니다.
         */
        Code.UNAVAILABLE -> DomainError.NetworkUnavailable

        /**
         * [Code.DEADLINE_EXCEEDED]
         * 발생 상황: 클라이언트가 설정한 타임아웃 시간 내에 서버가 응답하지 못했을 때 발생합니다. (주로 네트워크 상태 불안정)
         *
         */
        Code.DEADLINE_EXCEEDED -> DomainError.Timeout

        /**
         * [Code.PERMISSION_DENIED]
         * 발생 상황: Firestore Security Rules(보안 규칙)에 의해 읽기/쓰기 권한이 거부되었을 때 발생합니다.
         * 원인: 로그인하지 않은 사용자가 접근하거나, 규칙 로직상 허용되지 않은 필드를 수정하려 할 때 발생.
         */
        Code.PERMISSION_DENIED -> DomainError.Store.PermissionDenied

        /**
         * [Code.UNAUTHENTICATED]
         * 발생 상황: 요청에 유효한 인증 토큰이 없을 때 발생합니다.
         * 원인: 로그인이 안 된 상태이거나 토큰이 완전히 만료되어 갱신에 실패한 경우입니다.
         */
        Code.UNAUTHENTICATED -> DomainError.Store.Unauthenticated

        /**
         * [Code.NOT_FOUND]
         * 발생 상황: get() 요청을 보낸 특정 문서(Document)가 존재하지 않을 때 발생합니다.
         */
        Code.NOT_FOUND -> DomainError.Store.NotFound

        /**
         * [Code.ALREADY_EXISTS]
         * 발생 상황: 이미 존재하는 문서 경로에 set(data, SetOptions.merge()... 가 아닌) '생성' 요청을 보낼 때 발생합니다.
         */
        Code.ALREADY_EXISTS -> DomainError.Store.AlreadyExists

        /**
         * [Code.RESOURCE_EXHAUSTED]
         * 발생 상황:
         * 1. 무료 요금제(Spark)의 일일 읽기/쓰기 할당량을 모두 사용했을 때.
         * 2. 프로젝트의 결제 수단이 만료되어 서비스가 제한되었을 때.
         * 3. 특정 문서에 대한 초당 쓰기 속도 제한(초당 1회 권장)을 지속적으로 초과할 때.
         */
        Code.RESOURCE_EXHAUSTED -> DomainError.Store.QuotaExceeded

        else -> DomainError.Unexpected
    }

    is StorageException -> when (errorCode) {
        /**
         * [StorageException.ERROR_NOT_AUTHORIZED]
         * 발생 상황: Storage Security Rules에 의해 작업이 거부되었을 때 발생합니다.
         */
        StorageException.ERROR_NOT_AUTHORIZED -> DomainError.Storage.PermissionDenied

        /**
         * [StorageException.ERROR_QUOTA_EXCEEDED]
         * 발생 상황: 프로젝트의 저장 용량 또는 대역폭 할당량을 초과했을 때 발생합니다.
         */
        StorageException.ERROR_QUOTA_EXCEEDED -> DomainError.Storage.QuotaExceeded

        /**
         * [StorageException.ERROR_OBJECT_NOT_FOUND]
         * 발생 상황: 참조한 경로에 파일이 존재하지 않을 때 발생합니다.
         */
        StorageException.ERROR_OBJECT_NOT_FOUND -> DomainError.Storage.NotFound

        /**
         * [StorageException.ERROR_RETRY_LIMIT_EXCEEDED]
         * 발생 상황: 네트워크 문제 등으로 인해 재시도 횟수를 초과했을 때 발생합니다.
         */
        StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> DomainError.NetworkUnavailable

        else -> DomainError.Unexpected
    }

    else -> DomainError.Unexpected
}
