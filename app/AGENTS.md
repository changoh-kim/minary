# app/AGENTS.md

> **BLUF**
> `app` 모듈은 Minary의 Application Composition Root(조립 지점)이다. 앱 초기화, Firebase 구성, 최상위 Hilt 주입을 담당하며 **비즈니스/UI 로직 작성을 엄격히 금지**한다.

## 1. 초기화 순서 제약 (Startup Rules)
앱 시작 시 `MinaryApplication`에서 다음 순서로 초기화를 보장해야 한다.
1. `FirebaseApp.initializeApp` (가장 먼저)
2. `AppCheckInitializer` (Firestore 등 호출 전 필수)
3. Hilt Graph 준비
4. WorkManager Factory 제공 (Configuration.Provider 사용)

## 2. DI & Configuration 규칙
- **Hilt 우선순위**: 1. Constructor Injection, 2. @Binds, 3. @Provides 순으로 고려한다. 가능하면 Constructor Injection을 사용하고 @Provides 남용을 금지한다.
- **MinaryAppModule**: 전역 레벨(Context, ApplicationScope, GeminiApiKey 등)의 조립 및 전역 Qualifier 관리에만 사용한다.
- **Gemini**: `BuildConfig.GEMINI_API_KEY` 주입 시 반드시 `MinaryAppModule`의 `@GeminiApiKey` 한정자를 사용한다.

## 3. Firebase & App Check
- **Firebase**: `debug`(Debug App Check)와 `release`(Play Integrity) 소스셋을 완벽히 분리한다.
- **App Check**: 릴리즈 전 클라이언트와 서버 정책(`enforceAppCheck: true`) 동기화를 반드시 확인한다.
