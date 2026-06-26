package kr.co.core.common.error

sealed interface DomainError {

    // 1. 공통 에러
    data object NetworkUnavailable : DomainError  // 네트워크 문제(연결 끈김, 불안정, 방화벽등)
    data object Timeout : DomainError             // 네트워크 불안정으로 인한 시간 초과
    data object Unexpected : DomainError           // 예기치 않은 에러

    // 2. 도메인 특화 에러
    // auth
    sealed interface Auth : DomainError {
        data object InvalidCredentials : Auth   // 인증 정보 잘못 또는 형식 불일치(이메일, 비밀번호, 인증코드)
        data object UserNotFound : Auth         // 해당 이메일로 가입된 사용자를 찾을 수 없음
        data object EmailAlreadyInUse : Auth    // 이미 가입된 이메일 주소로 회원가입, 인증 수단 중복 연결
        data object WeakPassword : Auth         // 비밀번호 정책 미충족
        data object TooManyRequests : Auth      // 로그인, SMS인증, 비밀번호 변경과 같은 상황에서 무차별 대입 공격 발생시
        data object RequiresRecentLogin : Auth  // 재인증 필요 (계정 삭제, 이메일 변경 시)
        data object UserDisabled : Auth         // 관리자에 의해 비활성화된 계정
        data object TokenExpired : Auth         // ID Token 만료
    }

    // store
    sealed interface Store : DomainError {
        data object PermissionDenied : Store  // 권한 없음 (Security Rules)
        data object NotFound : Store          // 문서 없음
        data object QuotaExceeded : Store     // 할당량 초과
        data object AlreadyExists : Store     // 이미 존재하는 문서
        data object Unauthenticated : Store   // 인증 토큰 없음/만료
    }

    // storage
    sealed interface Storage : DomainError {
        data object PermissionDenied : Storage // 권한 없음 (Security Rules)
        data object QuotaExceeded : Storage    // 할당량 초과
        data object NotFound : Storage         // 파일 없음
    }

    // diary
    sealed interface Diary : DomainError {
        data object NotFound : Diary            // 저장된 일기 데이터를 찾을 수 없음
    }

    // time
    sealed interface Time : DomainError {
        data object NotInitialized : Time       // UTC 서버와 시간 동기화되지 않음
    }
}
