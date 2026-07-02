# :data Testing Guide

## Module Shape

```text
data/
├── di/
├── extension/
├── feature/
│   ├── account/
│   ├── calendar/
│   ├── dashboard/
│   ├── diary/
│   ├── emotion/
│   ├── profile/
│   ├── search/
│   ├── session/
│   ├── setting/
│   └── user/
└── service/
    ├── image/
    ├── network/
    ├── remoteconfig/
    └── time/
```

`:data`는 단일 Android library 모듈이다. 테스트는 `data/src/test`와
`data/src/androidTest`에 production package를 따라 배치한다.

## Test Layers

- `src/test`: mapper, `ExceptionMapper`, repository orchestration, Firebase provider delegation, RemoteConfig delegation, Generative AI parsing fallback, WorkManager scheduler request 생성 검증.
- `src/androidTest`: Room-backed local source, DataStore-backed local source, Android file/image 처리, Firebase Emulator integration, Hilt binding smoke, WorkManager/Hilt worker smoke.
- 테스트 순서는 `Mapper/ExceptionMapper` -> `Local/Remote DataSource` -> `RepositoryImpl` -> `SyncManager/Worker` -> `DI smoke` 순서로 확장한다.

## Current Coverage Snapshot

- JVM unit test는 mapper, `ExceptionMapper`, repository orchestration, local/remote source wrapper, sync/realtime manager, calendar repository/paging, time provider, RemoteConfig, test fake infrastructure를 커버한다.
- Android instrumented test는 Room/DataStore-backed local source, Android file/image 처리, network callback, WorkManager worker/scheduler, Firebase Emulator smoke/sync manager, Hilt binding smoke를 커버한다.
- `DataFeatureModuleHiltSmokeTest`는 `@BindValue` MockK 구현체로 domain contract binding만 좁게 검증한다. 실제 Firebase, Room, DataStore, WorkManager까지 포함한 app-level graph 검증은 `:app` smoke test 책임으로 둔다.
- `DataServiceModuleHiltSmokeTest`는 `ImageProcessorImpl`과 `NetworkMonitorImpl`처럼 `:data` 내부에서 실제 구현체를 안전하게 만들 수 있는 service binding을 검증한다.
- Firebase Emulator test는 opt-in이다. 일반 `connectedDebugAndroidTest`에서는 skip되며, emulator suite가 떠 있을 때 class filter와 `firebaseEmulatorEnabled=true`를 함께 사용한다.
- MockWebServer는 현재 production 코드에 Retrofit/OkHttp source가 없으므로 아직 의존성으로 추가하지 않는다. REST source가 생기는 Phase에서 이 문서의 기준에 맞춰 추가한다.

## Base Classes

- `BaseDataUnitTest`: JUnit5 coroutine unit test base. `Dispatchers.Main`을 `StandardTestDispatcher`로 교체하고 MockK state를 정리한다.
- `BaseDataInstrumentationTest`: AndroidJUnit4 coroutine instrumented test base. `ApplicationProvider` context와 `runDataAndroidTest`를 제공한다.
- `BaseDataHiltInstrumentationTest`: Hilt가 필요한 계측 테스트 전용 base. `HiltTestRunner`가 `HiltTestApplication`을 사용하도록 설정되어 있어야 하며, 실제 테스트 클래스에는 `@HiltAndroidTest`를 붙이고 필요하면 `@UninstallModules` 또는 `@BindValue`로 binding을 교체한다.
- `BaseFirebaseEmulatorTest`: Firebase Emulator Suite smoke/integration base. Android Studio 가상 디바이스는 기본 host `10.0.2.2`, 실제 디바이스는 `adb reverse` 기준 host `127.0.0.1`을 자동 선택한다. ports는 Auth `9099`, Firestore `8080`, Functions `5001`, Storage `9199`를 사용한다. 기본 `connectedDebugAndroidTest`에서는 skip되며, `firebaseEmulatorEnabled=true` runner argument를 넘긴 경우에만 실제 emulator에 연결한다.
- `BaseWorkManagerSchedulerTest`: WorkManager scheduler 계측 테스트 base. `work-testing`으로 test WorkManager를 초기화하고 `WorkSpec`을 조회해 unique work name, worker class, constraints, backoff, cancel 상태를 검증한다.

## Mocking Strategy

- MockK: Firebase provider, Generative AI SDK, RemoteConfig provider, WorkManager, scheduler, logger처럼 호출 모양과 side effect만 검증할 때 사용한다.
- Fake: Repository 테스트에서 local/remote source 상태와 Flow emission을 제어할 때 사용한다. Room/DataStore/Firebase 자체 동작을 Fake로 대체하지 않는다.
- 현재 source는 concrete/final class를 주입하므로 repository unit test에서는 `Fake...DataSource` wrapper의 `mock` 프로퍼티를 repository 생성자에 전달한다. 상태와 호출 기록은 Fake wrapper가 소유하고, concrete source 연결만 MockK가 담당한다.
- In-memory/file-backed infra: Room DAO transaction, relation, Flow invalidation, DataStore persistence, Android file 처리는 `src/androidTest`에서 실제 Android infra로 검증한다.
- Firebase Emulator: Security rules, Auth token, callable Functions, Firestore query/index, Storage metadata/download URL처럼 SDK와 server 계약이 핵심일 때 사용한다. 테스트는 test-only named `FirebaseApp`과 synthetic `.test` email을 사용한다.
- Firebase sync manager emulator test: 실제 Firestore document 생성, pull/push, LWW 비교처럼 SDK+rules+path 계약이 함께 맞아야 하는 분기는 emulator에서 검증한다. Local source는 Android fake wrapper로 고립해 Room/DataStore 동작을 반복 검증하지 않는다. Diary sync는 batch create/delete, month query, metadata state transition을 포함하고, Profile sync는 remote photo url 수신 시 local download hook 호출과 profile-photo field 보존까지 포함한다.
- Firebase emulator smoke는 `data/src/debug/AndroidManifest.xml`에서 debug/test 대상에만 cleartext traffic을 허용한다. Firebase SDK 형식 검증용 API key도 synthetic 값을 사용하며 실제 project credential을 테스트 코드에 넣지 않는다.
- MockWebServer: 현재 main 코드에는 Retrofit/OkHttp source가 없으므로 필수 인프라가 아니다. REST source가 추가되면 HTTP status, body, header, timeout, malformed JSON, connection drop 검증에 사용한다.

## Required Scenarios

- Mapper: `Dto/Entity/Proto/Model <-> Domain` 변환, null/default/unknown enum, timestamp, sync status, profile photo url, diary emotions, paging model 경계.
- Calendar paging source: `PagingSource.load()`를 직접 호출해 year/month key 계산, 1902년 시작 경계, diary merge, `SyncStatus.IDLE` 시 month sync 요청, 이미 `SYNCED`인 월의 no-op을 검증한다.
- `ExceptionMapper`: Firebase Auth/Functions/Firestore/Storage 코드별 `DomainError`, network unavailable, timeout, quota, permission, not found, unknown fallback.
- Remote source: blank input `require`, callable payload shape, response parsing, sign-out 호출, provider exception propagation. Account remote source는 create/check/delete callable payload, sign-in user mapping, delete account reload -> reauthenticate -> callable -> signOut 순서를 JVM unit test로 고정한다.
- Firebase emulator smoke: Auth로 synthetic 사용자를 만들고, 같은 uid의 Firestore diary path와 Storage profile photo path가 rules를 통과하는지 검증한다. Functions callable은 `system/service_control` seed 여부에 따라 성공 또는 `UNAVAILABLE` gate를 확인한다.
- Firebase sync manager emulator: remote 문서가 없으면 local settings/profile을 생성하고, diary pending create/delete는 Firestore batch로 반영한다. Remote가 더 최신이면 local로 pull하고, local이 더 최신이면 remote로 push하거나 month pull에서 remote를 skip하는 LWW 분기를 포함한다. Profile push는 사진 업로드 책임과 분리되어 있으므로 기존 remote `profilePhotoUrl`을 덮어쓰지 않는지도 검증한다.
- Local source: insert/update/delete/upsert, relation query, pending count, search paging, old diary deletion, sync metadata, Flow invalidation.
- Session/user storage source: FirebaseAuth current/reload/session-expired 분기, auth state listener close cleanup, app DataStore last uid pass-through, uid별 database/DataStore/internal directory 삭제, profile photo path 규칙, Storage download 실패 logging.
- Repository: source 호출 순서, `AppResult` 변환, known internal failure의 직접 `Err(DomainError...)`, Flow pass-through, scheduler/sync manager side effect.
- Dashboard repository: DAO 통계 조합, 90칸 heatmap 채움, week range, emotion count, longest streak, DAO 실패 시 `DomainError` 매핑과 logger 호출.
- Sync state repository: `StateFlow` 초기값/상태 변경, pending count Flow pass-through, initial/user data sync timestamp DataStore pass-through.
- Sync manager: LWW 비교, chunk size, pending create/update/delete, initial pull progress, month metadata `LOADING/SYNCED/FAILED`, realtime listener lifecycle.
- Realtime sync manager: JVM unit test에서 Firestore `EventListener`를 MockK로 capture한다. current user 없음 short-circuit, active 상태 중복 등록 방지, snapshot callback의 sync manager 위임, stop 시 `ListenerRegistration.remove()`, diary last pull timestamp 갱신 조건을 검증한다.
- Worker/scheduler: unique work name, worker class, constraints, backoff, enqueue/cancel, auth uid missing branch, retry/failure result. Full/periodic diary sync worker는 서버 시간 sync 호출, sync disabled short-circuit, `hasMore` 재예약, 완료 시 old diary cleanup branch를 포함한다.
- Time provider: `TrueTime.sync()`의 NTP host/timeout/cache/logging 설정, initialize 실패의 `DomainError` 매핑, initialized `now()`와 not-initialized fallback scheduling을 JVM unit test로 검증한다.
- DI smoke: data 단독 Hilt 테스트는 app-level binding이나 실제 Firebase/API key가 필요한 전체 graph를 한 번에 띄우지 않는다. 우선 service/domain contract binding처럼 좁은 graph를 `@UninstallModules`와 `@BindValue`로 고립해 검증하고, app graph 조립은 `:app` smoke test 책임으로 둔다.
- Android file/image service: 실제 cache file과 `ContentResolver` 기반 `file://` URI를 사용해 resize, no-upscale, EXIF rotation, invalid image 실패를 검증한다.
- Network service: 실제 네트워크 상태를 바꾸지 않고 `ConnectivityManager`를 MockK로 제어해 초기 active network, callback emission, `distinctUntilChanged`, unregister cleanup을 검증한다.

## Test Data Rules

- secret, credential, 실제 이메일 주소, 전화번호, 주소, 일기 본문, 원문 사용자 입력을 테스트명이나 fixture에 남기지 않는다.
- 식별자는 `uid-test`, `diary-id-test`, `title-test`처럼 비식별 synthetic 값을 사용한다.
- Firebase Emulator test는 실행 후 생성한 test data를 정리하고, 실제 production Firebase project를 대상으로 실행하지 않는다.

## Commands

기본 JVM 검증:

```bash
./gradlew :data:testDebugUnitTest
```

Android test source 컴파일:

```bash
./gradlew :data:compileDebugAndroidTestKotlin
```

일반 계측 테스트. Firebase Emulator test는 opt-in 조건이 없으면 skip된다.

```bash
./gradlew :data:connectedDebugAndroidTest
```

DI smoke만 빠르게 확인할 때:

```bash
./gradlew :data:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=kr.co.data.di.DataFeatureModuleHiltSmokeTest,kr.co.data.di.DataServiceModuleHiltSmokeTest
```

Firebase Emulator integration test는 `firebase-server`에서 emulator suite를 실행한 뒤 별도 경로로 수행한다. Android Studio 가상 디바이스는 host `10.0.2.2`를 사용한다.

```bash
./gradlew :data:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=kr.co.data.firebase.FirebaseEmulatorIntegrationSmokeTest,kr.co.data.feature.diary.sync.FirestoreDiarySyncManagerEmulatorTest,kr.co.data.feature.profile.sync.FirestoreUserProfileSyncManagerEmulatorTest,kr.co.data.feature.setting.sync.FirestoreUserSettingsSyncManagerEmulatorTest \
  -Pandroid.testInstrumentationRunnerArguments.firebaseEmulatorEnabled=true
```

실제 디바이스에서 실행할 때는 emulator port를 reverse한 뒤 host를 `127.0.0.1`로 고정한다.

```bash
adb reverse tcp:9099 tcp:9099
adb reverse tcp:8080 tcp:8080
adb reverse tcp:5001 tcp:5001
adb reverse tcp:9199 tcp:9199
./gradlew :data:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=kr.co.data.firebase.FirebaseEmulatorIntegrationSmokeTest,kr.co.data.feature.diary.sync.FirestoreDiarySyncManagerEmulatorTest,kr.co.data.feature.profile.sync.FirestoreUserProfileSyncManagerEmulatorTest,kr.co.data.feature.setting.sync.FirestoreUserSettingsSyncManagerEmulatorTest \
  -Pandroid.testInstrumentationRunnerArguments.firebaseEmulatorHost=127.0.0.1 \
  -Pandroid.testInstrumentationRunnerArguments.firebaseEmulatorEnabled=true
```

특수 네트워크 환경에서는 `firebaseEmulatorHost` runner argument로 host를 명시할 수 있다.

`firebaseEmulatorEnabled`를 넘기지 않으면 Firebase emulator smoke test는 skip되어 일반 계측 테스트 루프를 실패시키지 않는다.

경계 확인:

```bash
rg "kr\\.co\\.presentation|androidx\\.compose" data/src/main/java
rg ": Firebase(Auth|Firestore|Functions|Storage|RemoteConfig)[,)]" data/src/main/java -g "*.kt"
rg "android\\.util\\.Log|\\bLog\\.|printStackTrace\\(|println\\(" data/src/main/java -g "*.kt"
```
