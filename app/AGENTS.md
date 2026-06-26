# app/AGENTS.md

> **BLUF**
> `:app`은 Android Application composition root이다. 앱 실행에 필요한 최종 조립만 담당하고 UI shell이나 비즈니스 로직을 소유하지 않는다.

## 역할
- Application 초기화, app-level DI, build config 기반 최종 provider 조립을 담당한다.
- Firebase, App Check, WorkManager 등 앱 프로세스 단위 초기화를 담당한다.
- Android manifest, applicationId, google-services 적용처럼 application packaging 책임을 가진다.
- UI root와 navigation shell은 `:presentation`에 둔다.

## 의존성 규칙
- `:presentation`, `:data`, `:domain`, 필요한 `:core:*`만 참조한다.
- 공통 qualifier/module은 `:core:di`를 사용하고 `:app`에 중복 선언하지 않는다.
- `:app`에서 feature 구현체, repository, data source를 직접 호출하지 않는다.

## 패키지/코드 배치 규칙
- 앱 시작 초기화 순서는 `FirebaseApp.initializeApp` -> App Check 초기화 -> Hilt graph 준비 -> WorkManager factory 제공 순서를 유지한다.
- application scope에서만 필요한 DI provider를 둔다.
- Timber 초기화는 앱 시작시 `Application.onCreate()`에서 debug 빌드에만 수행하고, release 로그 수집이 필요하면 별도 정책으로 설계한다.
- Timber 초기화와 `AppLogger` 구현체/binding은 application composition root 책임이므로 `:app`에 둔다.
- `AppLogger` 구현은 wrapper로 인해 debug 로그의 실제 호출부 함수명과 라인 번호가 왜곡되지 않게 유지한다.
- DI는 Constructor Injection을 우선하고, `@Binds`, `@Provides`는 필요한 경우에만 사용한다.
- Android framework entry point와 앱 최종 설정 코드를 중심으로 유지한다.
- Firebase 관련 Gradle plugin과 google-services 처리는 application 모듈에서 최종 적용한다.
- WorkManager factory는 app-level 조립만 담당하고 worker 정책은 data/domain 쪽에 둔다.
- `BuildConfig.GEMINI_API_KEY`는 `:core:di`의 `GeminiApiKey` qualifier로만 제공한다.

## 금지사항
- Compose 화면, navigation graph, route, ViewModel을 두지 않는다.
- RepositoryImpl, SyncManager, Worker 내부 정책, DataSource 구현을 두지 않는다.
- feature별 비즈니스 규칙이나 UI 상태를 조립하지 않는다.
- secret 값을 코드, log, 문서 예시에 직접 작성하지 않는다.
- release 빌드에 Timber DebugTree를 plant하지 않는다. release 로그 수집이 필요하면 `AppLogger` 구현과 별도 Tree를 명시적으로 설계한다.

## 변경 시 체크리스트
- 앱 초기화 순서가 바뀌면 Firebase/App Check/WorkManager/Hilt 초기화 영향 범위를 확인한다.
- Timber Tree 또는 `AppLogger` 구현이 바뀌면 debug 로그에 실제 호출부 함수명과 소스 라인 번호가 계속 포함되는지 확인한다.
- app-level DI가 추가되면 해당 provider가 정말 application composition 책임인지 확인한다.
- UI shell 이동은 `:presentation`의 책임 경계 안에서만 허용한다.
- release/debug 설정 변경 시 Firebase, App Check, signing, BuildConfig 노출 범위를 함께 검증한다.
- App Check는 debug/release provider 분리를 유지하고, release 전 서버 `enforceAppCheck` 정책과 함께 검증한다.

## 권장 검증
- `./gradlew :app:assembleDebug`
- app-level DI 변경 시 `./gradlew :app:kaptDebugKotlin`
- Firebase/App Check 변경 시 Android client 설정과 `firebase-server` 정책을 함께 검증한다.
