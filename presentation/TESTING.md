# :presentation Testing Guide

## Module Shape

```text
presentation/
├── app/
│   ├── activity/
│   ├── mapper/
│   ├── model/
│   └── navigation/
├── feature/
│   ├── account/
│   ├── calendar/
│   ├── dashboard/
│   ├── diary/
│   ├── home/
│   ├── search/
│   └── setting/
└── res/
```

`:presentation`은 Jetpack Compose + Orbit MVI UI 계층이다. ViewModel은 domain UseCase만 호출하고, 화면은 state/action/side effect를 렌더링한다.

## Test Layers

- `src/test`: ViewModel state/side effect, mapper, formatter, 단순 `SavedStateHandle` key restore, `DomainError -> UiText` 분기를 JUnit5 + MockK + Turbine으로 검증한다.
- `src/androidTest`: Compose `Content` 함수의 입력, 버튼, dialog, loading/skeleton, action emission을 Compose UI Test로 검증한다.
- UI 테스트는 유지보수 비용이 크므로 ViewModel/mapper unit test를 먼저 넓히고, UI test는 핵심 화면과 컴포넌트만 얇게 둔다.

## Base Classes

- `BaseViewModelTest`: `Dispatchers.Main`을 `StandardTestDispatcher`로 교체하고 MockK state를 정리한다. `runPresentationTest` 안에서 ViewModel을 실제 인스턴스로 만들고 UseCase/AppLogger만 MockK로 대체한다.
- `BaseViewModelTest.stateFlow()`와 `sideEffectFlow()`는 Orbit container를 Turbine으로 수집하기 위한 helper다. 상태 검증은 `stateFlow().test { ... }`, side effect 검증은 `sideEffectFlow().test { ... }`를 기본으로 한다.
- `BaseComposeTest`: 테스트 전용 `ComposeTestActivity`와 `setMinaryContent {}`를 제공한다. 화면 테스트는 `MinaryTheme` 안에서 state 기반 content 함수를 직접 렌더링한다.
- `ComposeTestActivity`: 실제 디바이스와 Android Studio AVD에서 계측 테스트가 동일하게 돌도록 테스트 APK에서만 screen on, show when locked, keep screen on 플래그를 적용한다.
- Robolectric은 Android resource/semantics가 필요한 빠른 JVM smoke에만 사용한다. 실제 Compose runtime, focus/IME, animation, navigation callback 신뢰도가 필요하면 `src/androidTest`로 둔다.

## Mocking Strategy

- ViewModel은 실제 인스턴스로 테스트하고, domain UseCase와 `AppLogger`만 MockK로 격리한다.
- suspend UseCase는 `coEvery`, Flow UseCase는 `MutableStateFlow` 또는 `MutableSharedFlow`, command side effect는 `coVerify`/`verifyOrder`로 검증한다.
- `AppResult` 실패는 `Err(DomainError...)`로 만들고, ViewModel이 `UiText.StringResource`와 side effect로 변환하는지 검증한다.
- Compose content test는 ViewModel을 주입하지 않는다. state와 callback을 직접 넘겨 UI 렌더링과 action emission만 검증한다.
- `SavedStateHandle.toRoute()` 기반 typed navigation ViewModel은 Android `Bundle` 구현이 필요하므로 순수 JUnit5 테스트에 직접 올리지 않는다. 해당 화면은 route parsing을 분리 리팩터링하거나 Robolectric/JUnit4 또는 `src/androidTest`에서 검증한다.

## Required Scenarios

- Account: sign-in/sign-up validation, loading state, 성공 side effect, `DomainError`별 message resource, saved email/password restore, deletion confirmation.
- Diary: edit route 초기화, 신규/기존 diary load, title/content length limit, save success/failure side effect, detail load/delete dialog. 단, 현재 `SavedStateHandle.toRoute()` 의존 ViewModel은 JVM JUnit5가 아니라 Robolectric/계측 테스트 대상으로 둔다.
- Home: current user 없음 no-op, storage init 실패 critical state, sync enabled/disabled에 따른 start/stop 호출, retry action, realtime stop on clear.
- Search: recent search stream 반영, query trim/add, date range search, paging guard, diary click side effect.
- Settings/Profile: profile/settings stream 반영, sign-out success/failure, profile photo edit side effect, profile update validation.
- Calendar/Dashboard: route 기반 초기 상태, mapper 변환, load success/error, scroll side effect, empty/loading/success UI state.

## Decision Guide

- Unit Test를 쓴다: ViewModel state, side effect, UseCase 호출 순서, validation, mapper, formatter, `DomainError -> UiText`를 검증할 때.
- Compose UI Test를 쓴다: 사용자가 입력/클릭하는 interaction, dialog/loading visibility, semantics, content branching이 핵심일 때.
- Robolectric을 쓴다: Android resource 접근이 필요한 JVM smoke가 필요하지만 실제 디바이스 fidelity가 중요하지 않을 때.
- Instrumented Test를 쓴다: 실제 Compose runtime, Activity, focus/IME, animation, navigation callback 신뢰도가 필요할 때.

## Test Data Rules

- secret, credential, 실제 이메일 주소, 전화번호, 주소, 일기 본문, 원문 사용자 입력을 테스트명이나 fixture에 남기지 않는다.
- 식별자는 `uid-test`, `title-test`, `content-test`, `user-test@example.test`처럼 synthetic 값을 사용한다.
- UI 문자열은 가능하면 string resource 또는 content description으로 찾고, 불안정한 노드에는 최소한의 stable `testTag`를 추가한다.

## Commands

PR fast loop:

```bash
./gradlew :presentation:testDebugUnitTest
./gradlew :presentation:compileDebugAndroidTestKotlin
```

한 번에 실행:

```bash
./gradlew :presentation:testDebugUnitTest :presentation:compileDebugAndroidTestKotlin
```

UI 계측 테스트는 실제 디바이스나 Android Studio AVD가 연결된 상태에서 실행한다:

```bash
./gradlew :presentation:connectedDebugAndroidTest
```

경계 확인:

```bash
rg "kr\\.co\\.data|com\\.google\\.firebase|kr\\.co\\.minary" presentation/src/main/java
rg "android\\.util\\.Log|\\bLog\\.|printStackTrace\\(|println\\(" presentation/src/main/java -g "*.kt"
rg "Timber\\." presentation/src/main/java -g "*.kt"
```

## CI/CD Operation

- PR 기본 검증은 `.github/workflows/presentation-tests.yml`의 `Presentation fast checks` job으로 고정한다.
- PR job은 `:presentation:testDebugUnitTest`, `:presentation:compileDebugAndroidTestKotlin`, presentation boundary/log rule 검색만 실행한다.
- Compose connected UI test는 PR 기본 job에 넣지 않는다. 유지보수 비용과 실행 시간을 줄이기 위해 `workflow_dispatch`에서 `run_connected=true`로 수동 실행한다.
- 수동 connected job은 AVD에서 `:presentation:connectedDebugAndroidTest`를 실행하고, 실패 여부와 무관하게 Android test report를 artifact로 보존한다.
- 로컬에서는 실제 디바이스나 Android Studio AVD 둘 다 사용할 수 있다. 화면이 꺼져 있거나 잠금 화면이어도 `ComposeTestActivity`가 테스트 APK에서만 화면 켜기와 잠금 위 표시를 처리한다.
- UI test가 흔들리면 기다림을 늘리기보다 state 기반 `Content` 테스트로 축소하거나 stable `testTag`를 최소 추가한다.
- animation/focus/IME처럼 실제 runtime 의존성이 있는 검증만 connected test에 남기고, ViewModel 상태와 side effect는 JVM unit test로 이동한다.
