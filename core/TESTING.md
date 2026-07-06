# Core Testing Guide

> `:core` 테스트는 pure Kotlin 공통 계약, Room/DataStore/local file/Firebase provider 기반, Compose UI support가 각 모듈 책임 안에서 안정적으로 동작하는지 검증한다.

## Module Tree

```text
core/
├── common
├── di
├── storage
├── database
├── datastore
├── firebase
└── ui/
    ├── common
    └── design
```

## Unit Test Targets

- `:core:common`: enum fallback, converter extension, `AppResult<DomainError>`, shared state.
- `:core:database`: Room converter only. DAO/query/transaction은 androidTest에서 검증한다.
- `:core:datastore`: Proto serializer, corruption handling, Preferences DataStore wrapper with Fake `DataStore`.
- `:core:storage`: `LocalStoragePathProvider` name/path contract.
- `:core:firebase`: Provider delegation and Firebase path/callable/storage-reference contracts with MockK.
- `:core:di`: dispatcher/scope provider direct-call checks.
- `:core:ui:common`: `LoadState`, `SafeLoad`, `UiText` equality.

## Instrumented Test Targets

- `:core:database`: in-memory Room DAO, transaction, relation, cascade, Flow invalidation, recent search limit, metadata DAO, user DB provider.
- `:core:datastore`: Context file-backed DataStore providers, uid cache, delete behavior, Proto/Preferences persistence.
- `:core:storage`: `UserInternalStorageProvider` directory creation, uid switching, deletion.
- `:core:firebase`: emulator-configured SDK provider smoke. RemoteConfig는 emulator 대상에서 제외하고 unit test로 검증한다.
- `:core:ui:common`: `Context.getString(UiText)`, `LocalDateParceler`, resource lookup, Compose branch rendering.
- `:core:ui:design`: Compose component semantics and theme smoke.

## Decision Rules

- Android `Context`, `Parcel`, `Resources`, SQLite engine, DataStore file lock, Firebase emulator, or Compose semantics가 필요하면 `src/androidTest`.
- pure Kotlin value conversion, serializer byte IO, provider delegation, coroutine state transition은 `src/test`.
- collaborator 호출 검증은 MockK를 사용한다.
- 상태 저장 또는 Flow source 대체가 필요하면 Fake를 사용한다.
- Room/DataStore 자체 동작을 검증하는 테스트에서는 Fake가 아니라 실제 in-memory/file-backed infra를 사용한다.

## Base Test

- JVM 테스트는 각 모듈의 `*.testing.BaseUnitTest`를 사용한다.
- 계측 테스트는 각 모듈의 `*.testing.BaseInstrumentationTest`를 사용한다.
- 두 base 모두 `StandardTestDispatcher`, `Dispatchers.setMain`, `Dispatchers.resetMain`, `clearAllMocks()`를 기본으로 한다.

## Verification

```bash
./gradlew :core:common:test \
  :core:database:testDebugUnitTest \
  :core:datastore:testDebugUnitTest \
  :core:storage:testDebugUnitTest \
  :core:firebase:testDebugUnitTest \
  :core:di:testDebugUnitTest \
  :core:ui:common:testDebugUnitTest

./gradlew :core:database:compileDebugAndroidTestKotlin \
  :core:datastore:compileDebugAndroidTestKotlin \
  :core:storage:compileDebugAndroidTestKotlin \
  :core:firebase:compileDebugAndroidTestKotlin \
  :core:di:compileDebugAndroidTestKotlin \
  :core:ui:common:compileDebugAndroidTestKotlin \
  :core:ui:design:compileDebugAndroidTestKotlin
```

Firebase emulator smoke를 실제 실행하려면 `firebase-server`에서 Emulator Suite를 먼저 실행한다. Android emulator에서는 host `10.0.2.2`와 `firebase-server/firebase.json`의 포트를 사용한다.

## Local CI Reproduction

GitHub Actions의 `Android Instrumented Tests` 실패를 로컬에서 재현할 때는 Android Studio AVD 또는 실제 디바이스를 먼저 실행한 뒤 repository root에서 아래 script를 실행한다.

GitHub Actions의 `Android Fast Checks`와 같은 빠른 JVM/compile/build 검증은 아래 script로 실행한다.

```bash
./scripts/ci/run-android-fast-local.sh
```

`Android Instrumented Tests`는 Android Studio AVD 또는 실제 디바이스를 먼저 실행한 뒤 repository root에서 아래 script를 실행한다.

```bash
./scripts/ci/run-android-instrumented-local.sh
```

특정 실패 테스트만 빠르게 확인할 때는 class filter를 사용한다.

```bash
./gradlew :core:ui:design:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=kr.co.core.ui.design.theme.MinaryThemeInstrumentedTest
```

AVD는 GitHub Actions의 Android emulator와 가장 가까운 재현 환경이다. 실제 디바이스는 제조사/OS 설정 차이가 있으므로, CI 실패 재현은 가능하면 AVD에서 먼저 확인한다.
