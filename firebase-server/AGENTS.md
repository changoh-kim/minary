# firebase-server/AGENTS.md

> **BLUF**
> `firebase-server`는 Cloud Functions, Security Rules, 비용 방어 정책을 담당하는 서버 방어선이다. 개인정보 보호와 Firestore 비용 제어를 최우선으로 한다.

## 역할
- Cloud Functions, Firestore/Storage Rules, budget 대응 로직을 관리한다.
- Android client와 공유되는 Firebase 경로 및 보안 계약을 서버 측에서 보호한다.
- App Check, auth, payload 검증을 통해 비정상 호출과 비용 폭증을 방지한다.

## 의존성 규칙
- 서버 코드는 Android client 모듈에 의존하지 않는다.
- 공유 경로나 query shape가 바뀌면 `:core:firebase`와 `:data` 구현을 함께 검증한다.
- secret은 Firebase Secret Manager 등 서버용 secret 관리 방식을 사용한다.
- client 신뢰가 아니라 server-side 검증을 기준으로 보안 정책을 작성한다.

## 패키지/코드 배치 규칙
- 함수는 region, timeout, retry, idempotency 정책을 명시해 작성한다.
- 모든 Cloud Function은 `asia-northeast3` 리전을 기본으로 사용한다.
- rules는 소유권 검증과 query shape를 기준으로 관리한다.
- 비용 방어 로직은 우회가 어렵도록 server-side에서 검증한다.
- 대량 삭제/정리 작업은 pagination과 batch limit을 반드시 적용한다.
- 민감 값은 `defineSecret` 등 Firebase Secret Manager 기반으로 관리한다.

## 금지사항
- 이메일, 일기 본문 등 PII를 로그에 남기지 않는다.
- trigger update에서 변경 여부 확인 없이 재귀 호출 가능성을 만들지 않는다.
- 인증, App Check, payload 검증을 client 신뢰만으로 대체하지 않는다.
- 대량 삭제/정리 작업을 pagination 없이 실행하지 않는다.
- Storage rules에서 프로필 이미지 업로드의 size/content-type 검증을 제거하지 않는다.

## 변경 시 체크리스트
- Firestore/Storage 경로 변경 시 Android client provider, data 구현, rules/functions를 함께 검증한다.
- App Check enforce 정책 변경 시 Android debug/release 설정을 함께 검증한다.
- rules 변경 후 필요한 index와 client query shape를 확인한다.
- budget/kill switch 정책 변경 시 client service status 처리와 함께 검증한다.
- 서비스 차단 정책 변경 시 `assertServiceAvailable`과 Android remote config/service status 처리를 함께 검증한다.

## 권장 검증
- Firebase rules/functions 변경 시 Firebase emulator 또는 배포 전 dry-run 절차를 사용한다.
- 경로 문자열 변경 시 Android `:core:firebase`, `:data` 검색 결과를 함께 검증한다.
- secret 또는 runtime config 변경 시 실제 값이 repo에 커밋되지 않았는지 검증한다.
