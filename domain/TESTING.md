# Domain Testing Guide

> `:domain` 테스트는 Offline-first/LWW 정책, `AppResult<DomainError>` 실패 계약, Flow 상태 전이, sync orchestration을 JVM unit test로 고정한다.

## Scope

- 테스트 러너는 JUnit5(Jupiter)를 사용한다.
- coroutine/Flow 테스트는 `kotlinx-coroutines-test`의 `runTest`, `StandardTestDispatcher`를 사용한다.
- 복잡한 호출 순서와 side-effect 검증은 MockK를 사용한다.
- repository/state repository처럼 데이터 소스 역할을 하는 계약은 Fake 구현을 기본으로 사용한다.
- Android, Firebase, Room, DataStore, WorkManager 구현체는 `:domain` 테스트에 직접 등장하지 않는다.

## Test Units

현재 production `UseCase`는 모두 최소 1개 이상의 테스트 파일에서 다룬다. 단일 책임 UseCase는 `SomeUseCaseTest` 파일을 두고, stop/all-stop처럼 같은 scheduler 계약을 공유하는 얇은 UseCase는 `Stop...UseCasesTest` 그룹 파일에 묶을 수 있다.

Repository/service/sync 계약 인터페이스 자체는 단독 테스트하지 않는다. 해당 계약은 UseCase 테스트에서 Fake 또는 MockK 구현으로 입력 전달, 상태 전이, 호출 순서, 실패 전파를 검증한다.

### 순수 계산

- `CalendarGenerator`
  - 월 grid가 항상 42칸인지 검증한다.
  - 현재월/이전월/다음월 `isCurrentMonth` flag를 검증한다.
  - 일요일 시작 월, 윤년 2월, 연도/월 목록의 ascending/descending/size를 검증한다.

### 정책/변환 UseCase

- `CreateDiaryUseCase`, `UpdateDiaryUseCase`, `DeleteDiaryUseCase`
  - `ServerTimeProvider.now()`가 `createdAt`/`updatedAt`에 반영되는지 검증한다.
  - `DiarySyncStatus.PENDING_CREATE`, `PENDING_UPDATE`, `PENDING_DELETE`가 강제되는지 검증한다.
  - repository `Err(DomainError...)`가 그대로 전파되는지 검증한다.
- `UpdateUserProfileUseCase`, `UpdateUserProfilePhotoUseCase`, `UpdateAppThemeUseCase`, `UpdateDiarySyncEnabledUseCase`
  - current uid와 서버 시간이 repository 입력에 반영되는지 검증한다.
  - session 실패 시 update repository가 호출되지 않는지 검증한다.
  - update repository 실패가 그대로 전파되는지 검증한다.
- `AddRecentSearchUseCase`
  - blank query는 repository를 호출하지 않는지 검증한다.
  - query trim 결과와 서버 시간이 repository에 전달되는지 검증한다.
  - repository 예외는 예외로 전파되는지 검증한다.
- `GetDiariesByDateRangeStreamUseCase`
  - center month 기준 `minusMonths(range).atDay(1)`부터 `plusMonths(range).atEndOfMonth()`까지 계산되는지 검증한다.

### Orchestration UseCase

- `SignInUseCase`
  - sign-in 성공 후 user data sync 성공 시 `Ok(Account)`를 반환한다.
  - sign-in 실패 시 sync를 호출하지 않는다.
  - sync 실패 시 `accountService.signOut()` 보상 동작을 호출하고 sync 실패를 반환한다.
- `SignOutUseCase`
  - diary full/periodic sync와 realtime sync들을 중지한 뒤 `accountService.signOut()` 결과를 반환한다.
  - immediate sync는 취소하지 않는 현재 정책을 검증한다.
- `DeleteAccountUseCase`
  - 계정 삭제 실패 시 sync stop/storage cleanup을 호출하지 않는다.
  - 계정 삭제 성공 시 diary/profile/settings sync를 모두 중지하고 반환 uid의 storage를 삭제한다.
  - storage 삭제 실패는 `Err`로 전파된다.
- `StartUserDataSyncUseCase`
  - 마지막 sync가 1시간 이내면 profile/settings sync를 호출하지 않는다.
  - sync 실행 시 `InProgress.Indeterminate`에서 `Completed`로 상태가 전이되고 timestamp가 저장된다.
  - profile/settings 각각의 실패가 `Failed(error)` 상태와 `Err(error)`로 전파된다.
  - profile/settings sync가 같은 invocation 안에서 모두 실행되는지 검증한다.
- `InitUserStorageUseCase`
  - 이전 uid가 없으면 삭제 없이 current uid만 저장한다.
  - 이전 uid가 current uid와 같으면 삭제하지 않는다.
  - 이전 uid가 다르면 이전 uid storage를 삭제하고 current uid를 저장한다.
- `InitialDiarySyncUseCase`
  - 시작 시 `InProgress.Determinate(0f)`를 기록한다.
  - `performInitialPull` progress callback을 상태로 반영한다.
  - 성공 시 initial sync completed flag와 `Completed` 상태를 기록한다.
  - session 실패 또는 pull 실패 시 `Failed(error)` 상태를 기록한다.
- `StartDiarySyncUseCase`
  - initial sync 미완료면 `InitialDiarySyncUseCase`를 먼저 호출한다.
  - initial sync 실패 시 periodic sync를 예약하지 않는다.
  - initial sync 완료 상태면 full sync와 periodic sync를 예약한다.
- `RequestMonthSyncUseCase`
  - current session uid와 target `YearMonth`가 repository로 전달되는지 검증한다.
  - session/repository 실패가 그대로 전파되는지 검증한다.

### 얇은 위임/Flow UseCase

- `GetDiaryUseCase`, `GetDiaryStreamUseCase`, `GetPagedDiariesUseCase`, `GetDiaryChangeEventUseCase`
- `GetMonthSyncStatusStreamUseCase`, `GetInitDiarySyncStateStreamUseCase`, `CheckDiarySyncStateUseCase`
- `GetUserProfileUseCase`, `GetUserProfileStreamUseCase`
- `GetUserSettingsStreamUseCase`, `GetAppThemeStreamUseCase`, `GetDiarySettingsStreamUseCase`
- `GetRecentSearchesUseCase`, `RemoveRecentSearchUseCase`, `ClearAllRecentSearchesUseCase`
- `GetSessionStateStreamUseCase`, `GetCurrentUserUseCase`, `ReloadSessionUseCase`
- `GetCalendarMonthUseCase`, `GetCalendarYearUseCase`
- `GetDashboardUseCase`, `GetNetworkStatusStreamUseCase`
- `CheckEmailAvailabilityUseCase`, `CreateAccountUseCase`, `DeleteUserStorageUseCase`
- `SyncServerTimeUseCase`, `CheckServiceStatusUseCase`
- realtime/scheduler start-stop UseCase들

얇은 UseCase도 public contract이므로 최소 1개 이상의 pass-through 테스트를 둔다. Flow UseCase는 upstream Flow와 동일한 값을 방출하는지, `distinctUntilChanged()`가 있는 경우 중복 emission을 제거하는지 검증한다.

## Mocking Strategy

### Fake를 우선 사용할 대상

데이터 상태를 갖거나 Flow를 제공하는 계약은 Fake로 테스트한다.

- `FakeDiaryRepository`
- `FakeSessionRepository`
- `FakeUserSettingsRepository`
- `FakeUserProfileRepository`
- `FakeSearchRepository`
- `FakeDiarySyncStateRepository`
- `FakeUserDataSyncStateRepository`
- `FakeUserStorageRepository`
- `FakeServerTimeProvider`

Fake는 구현 세부사항을 재현하지 않는다. 테스트가 검증할 입력을 캡처하고, `AppResult`/Flow/state를 테스트가 원하는 값으로 제어하는 용도로만 둔다.

### MockK를 사용할 대상

명령형 side-effect, 호출 순서, 보상 동작이 중요한 계약은 MockK를 사용한다.

- `AccountService`
- sync manager/scheduler/realtime scheduler 계약
- `RemoteConfigRepository`
- `ServerTimeSyncScheduler`
- nested UseCase 의존성

호출 순서가 정책인 경우 `coVerifySequence` 또는 `verifySequence`를 사용한다. 순서가 정책이 아니면 `coVerify`/`verify`로 필요한 호출만 검증한다.

## Base Test Class

공용 coroutine 환경은 `kr.co.domain.testing.DomainCoroutineTest`를 상속한다.

```kotlin
class SomeUseCaseTest : DomainCoroutineTest() {
    @Test
    fun `test name`() {
        runDomainTest {
            // arrange
            // act
            // assert
        }
    }
}
```

- `runDomainTest` 안에서 suspend UseCase와 Flow를 검증한다.
- `Dispatchers.Main`은 `StandardTestDispatcher`로 교체된다.
- 각 테스트 후 `Dispatchers.resetMain()`과 `clearAllMocks()`가 실행된다.
- 시간 의존성은 `FakeServerTimeProvider`로 고정하고, 기기 시간을 기대값으로 사용하지 않는다.

## Assertions And Fixtures

- `DomainResultAssertions.kt`
  - `result.assertOk()`
  - `result.assertOk(expected)`
  - `result.assertErr()`
  - `result.assertErr(expected)`
- `DomainFixtures.kt`
  - `uid-test`, `uid-other`, `email-test` 같은 비식별 fixture만 사용한다.
  - 실제 이메일 주소, credential, 일기 본문, 사용자 입력 원문을 테스트 이름이나 문서 예시에 남기지 않는다.
  - 일기 본문이 필요하지 않으면 `content = ""`를 기본값으로 둔다.

## Flow Testing

- 단일 emission 검증은 `first()`를 사용한다.
- 여러 emission 검증은 `take(n).toList()`를 사용한다.
- 테스트 double은 `MutableStateFlow` 또는 `MutableSharedFlow`를 사용한다.
- `distinctUntilChanged()`가 있는 UseCase는 같은 값을 연속 emit한 뒤 결과 emission 수를 검증한다.
- Flow collection이 끝나지 않는 구조라면 `take(n)`으로 종료 조건을 명시한다.

## Required Scenarios By Feature

### Account

| UseCase | 필수 케이스 |
| --- | --- |
| `SignInUseCase` | 로그인 성공+sync 성공, 로그인 실패 short-circuit, sync 실패 시 `signOut()` 보상 |
| `CreateAccountUseCase` | `joinedAt` 서버시간 주입, account service 실패 전파 |
| `SignOutUseCase` | full/periodic/realtime 중지 후 signOut, signOut 실패 전파 |
| `DeleteAccountUseCase` | delete 실패 시 cleanup 미호출, 성공 시 전체 sync 중지와 storage 삭제, storage 실패 전파 |
| `CheckEmailAvailabilityUseCase` | `Ok(true/false)` pass-through, `Err` pass-through |

### User

| UseCase | 필수 케이스 |
| --- | --- |
| `StartUserDataSyncUseCase` | 1시간 gate short-circuit, 성공 상태 전이와 timestamp 저장, profile 실패, settings 실패 |
| `InitUserStorageUseCase` | 이전 uid 없음, 이전 uid 같음, 이전 uid 다름 |
| `DeleteUserStorageUseCase` | uid 전달, `Ok`/`Err` pass-through |

### Diary

| UseCase | 필수 케이스 |
| --- | --- |
| `CreateDiaryUseCase` | created/updated 시간 고정, `PENDING_CREATE`, repository 실패 전파 |
| `UpdateDiaryUseCase` | updated 시간 고정, `PENDING_UPDATE`, repository 실패 전파 |
| `DeleteDiaryUseCase` | updated 시간 고정, `PENDING_DELETE`, repository 실패 전파 |
| `GetDiaryUseCase` | date 전달, null diary, repository 실패 전파 |
| `GetDiaryStreamUseCase` | date 전달, Flow emission |
| `GetPagedDiariesUseCase` | query/date/limit/offset 전달, 실패 전파 |
| `GetDiariesByDateRangeStreamUseCase` | 기본 range=1, custom range, month boundary 계산 |
| `GetDiaryChangeEventUseCase` | repository event Flow pass-through |

### Diary Sync

| UseCase | 필수 케이스 |
| --- | --- |
| `InitialDiarySyncUseCase` | 0f 시작, progress 반영, 완료 flag, session 실패, pull 실패 |
| `StartDiarySyncUseCase` | initial 미완료 성공, initial 실패, initial 완료 후 full+periodic 예약 |
| `RequestMonthSyncUseCase` | session uid 전달, session 실패, repository 실패 |
| `CheckDiarySyncStateUseCase` | pending 0이면 false, pending 양수면 true |
| `StartRealtimeDiarySyncUseCase` | start 호출 후 `Ok(Unit)`, start 예외 전파 |
| `StopRealtimeDiarySyncUseCase` | stop 호출 |
| `StopAllDiarySyncUseCase` | all sync cancel + realtime stop |
| `StopDiaryFullSyncUseCase` | full sync cancel |
| `StopDiaryPeriodicSyncUseCase` | periodic sync cancel |
| `GetInitDiarySyncStateStreamUseCase` | state Flow pass-through |
| `GetMonthSyncStatusStreamUseCase` | yearMonth 전달과 status Flow pass-through |

### Profile And Settings

| UseCase | 필수 케이스 |
| --- | --- |
| `UpdateUserProfileUseCase` | uid/server time 주입, session 실패 시 repository 미호출, update 실패 전파 |
| `UpdateUserProfilePhotoUseCase` | uid/photoUrl/server time 전달, session 실패, update 실패 |
| `GetUserProfileUseCase` | 첫 emission 반환, 실패 emission 반환 |
| `GetUserProfileStreamUseCase` | stream pass-through, duplicate 제거 |
| `UpdateAppThemeUseCase` | uid/theme/server time 전달, session 실패, update 실패 |
| `GetAppThemeStreamUseCase` | duplicate 제거 |
| `GetUserSettingsStreamUseCase` | stream pass-through, duplicate 제거 |
| `UpdateDiarySyncEnabledUseCase` | uid/enabled/server time 전달, session 실패, update 실패 |
| `GetDiarySettingsStreamUseCase` | enabled Flow pass-through |
| profile/settings realtime start-stop/all-stop | scheduler 호출과 예외 전파 |

### Search

| UseCase | 필수 케이스 |
| --- | --- |
| `AddRecentSearchUseCase` | blank 무시, trim+server time 전달, repository 예외 전파 |
| `GetRecentSearchesUseCase` | Flow pass-through |
| `RemoveRecentSearchUseCase` | query 전달, repository 예외 전파 |
| `ClearAllRecentSearchesUseCase` | clear 호출, repository 예외 전파 |

### Calendar, Session, Dashboard, Service

| UseCase/Unit | 필수 케이스 |
| --- | --- |
| `CalendarGenerator` | 42칸, 현재월 flag, 일요일 시작 월, 윤년 2월, ascending/descending/size |
| `GetCalendarMonthUseCase` | `YearMonth` 전달 |
| `GetCalendarYearUseCase` | `Year` 전달 |
| `GetSessionStateStreamUseCase` | Flow pass-through |
| `GetCurrentUserUseCase` | current user `Ok`/`Err` pass-through |
| `ReloadSessionUseCase` | reload `Ok`/`Err` pass-through |
| `GetDashboardUseCase` | dashboard `Ok`/`Err` pass-through |
| `GetNetworkStatusStreamUseCase` | online/offline Flow pass-through |
| `SyncServerTimeUseCase` | sync 성공 시 scheduler 미호출, sync 실패 시 `scheduleSync()` |
| `CheckServiceStatusUseCase` | fetch 후 active, fetch 후 maintenance reason |

## Verification

테스트 인프라나 domain 테스트를 바꾼 뒤 최소 아래 명령을 실행한다.

```bash
./gradlew :domain:test
rg "android\\.|androidx\\.compose|Firebase|Room|WorkManager|DataStore" domain/src/main/java
rg "Timber|android\\.util\\.Log|\\bLog\\.|printStackTrace\\(|println\\(" domain/src/main/java -g "*.kt"
```

계약 변경이 data/presentation에 영향을 주면 아래도 실행한다.

```bash
./gradlew :data:compileDebugKotlin :presentation:compileDebugKotlin
```
