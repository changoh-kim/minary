# core/firebase/AGENTS.md

> **BLUF**
> `:core:firebase`는 Firebase SDK provider와 공통 Firebase 설정 전용 모듈이다. Firebase 기반 data 구현 로직은 `:data`에 둔다.

## 역할
- Firebase Auth, Firestore, Functions, Storage, RemoteConfig SDK 인스턴스의 공통 provider를 제공한다.
- Android client와 server가 공유하는 Firebase 경로 계약의 기준점을 제공한다.
- data 모듈이 Firebase 구현을 작성할 수 있는 얇은 기반을 제공한다.

## 의존성 규칙
- Firebase provider에 필요한 의존성만 허용한다.
- `:data`, `:presentation`, `:domain` 구현 세부사항을 참조하지 않는다.
- UI, repository, sync orchestration 의존성을 추가하지 않는다.
- Firebase 경로 계약이 변경되면 server rules/functions와 동시 검증한다.

## 패키지/코드 배치 규칙
- provider는 얇게 유지하고 query/business logic을 포함하지 않는다.
- Auth provider는 현재 사용자 조회, 로그인/로그아웃 호출, auth state listener 연결처럼 SDK 접근점만 제공한다.
- RemoteConfig provider는 fetch/activate, fetch interval 설정, key 기반 값 조회 같은 SDK 접근점만 제공한다.
- 경로 상수는 여러 구현에서 공유되는 계약일 때만 둔다.
- 테스트 대체가 가능하도록 SDK 접근 지점을 명확히 유지한다.
- path naming은 Android client와 `firebase-server`가 같은 의미로 해석할 수 있게 유지한다.

## 금지사항
- RepositoryImpl, RemoteDataSource, SyncManager를 두지 않는다.
- 로그인 정책, 세션 만료 처리, 재인증 흐름 같은 auth business logic을 provider에 넣지 않는다.
- maintenance, sync enable, AI enable 같은 RemoteConfig 정책 해석을 provider에 넣지 않는다.
- Firestore query 정책이나 Cloud Functions payload mapping을 두지 않는다.
- Domain/UI model 변환을 두지 않는다.
- Firebase read/write 비용 정책을 provider에 숨기지 않는다.

## 변경 시 체크리스트
- Firestore/Storage 경로 변경 시 `:data` 구현과 `firebase-server` rules/functions를 함께 검증한다.
- Firebase SDK provider 변경 시 app 초기화, DI graph, data 사용처 영향을 확인한다.
- Auth provider API 변경 시 account/session/sync worker/realtime sync 사용처를 함께 갱신한다.
- RemoteConfig provider API 변경 시 remote config repository와 service status 처리 흐름을 함께 갱신한다.
- server 계약과 달라질 수 있는 문자열은 문서와 코드 양쪽에서 검증한다.

## 권장 검증
- `./gradlew :core:firebase:compileDebugKotlin`
- 경로 변경 확인: `rg "users|diaries|settings|profile|storage|firestore" core/firebase data firebase-server`
- 의존성 경계 확인: `rg "kr\\.co\\.data|kr\\.co\\.presentation|kr\\.co\\.domain" core/firebase/src/main/java`
