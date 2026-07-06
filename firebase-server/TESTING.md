# firebase-server 테스트 가이드

## 목적

`firebase-server` 테스트는 Android client가 의존하는 서버 계약을 검증한다. 여기에는 Firestore Rules,
Storage Rules, Cloud Functions callable/trigger, service control, budget 방어 로직, emulator 실행
환경이 포함된다.

Android 쪽 repository/sync 정책은 `:data` 테스트 책임이다. 이 문서는 서버가 어떤 emulator 상태로
실행되어야 하는지, rules/functions 변경 시 어떤 계약을 확인해야 하는지에 집중한다.

## 현재 구조

```text
firebase-server/
├── firebase.json                 # Firestore/Storage/Functions/Auth emulator 설정
├── firestore.rules               # Firestore 소유권/경로 rules
├── storage.rules                 # Storage profile photo rules
├── firestore.indexes.json        # Firestore index 계약
├── remoteconfig.template.json    # Remote Config template
├── emulator-data/                # 선택적 emulator import/export baseline
└── functions/
    ├── src/index.ts              # callable, trigger, budget/service control functions
    ├── package.json              # lint/build/serve/deploy scripts
    └── tsconfig.json
```

## Emulator 역할

Firebase Emulator Suite는 production Firebase project를 건드리지 않고 Android client와 서버 계약을
검증하기 위해 사용한다.

- Auth emulator: synthetic test user 생성과 auth token 기반 rules 검증.
- Firestore emulator: `/users/{uid}/profile`, `/users/{uid}/settings`, `/users/{uid}/diaries` 소유권 검증.
- Storage emulator: `profile_photos/{uid}.jpg` 읽기/쓰기, content type, size rule 검증.
- Functions emulator: callable payload, service control gate, account cleanup, budget/service control function smoke 검증.
- Pub/Sub emulator: budget alert function처럼 Pub/Sub trigger가 필요한 서버 흐름 검증.

## 기본 실행

서버 변경 전후에는 functions lint/build를 먼저 확인한다.

```bash
cd firebase-server
npm --prefix functions run lint
npm --prefix functions run build
```

Android `:data` Firebase Emulator integration test와 함께 사용할 때는 전체 관련 emulator를 실행한다.

```bash
./scripts/ci/start-firebase-emulator-suite-local.sh
```

`functions/package.json`의 `serve` 스크립트는 현재 functions emulator만 실행한다.

```bash
cd firebase-server
npm --prefix functions run serve
```

이 명령은 callable/trigger 함수만 단독 확인할 때는 유용하지만, Android `:data` emulator integration test는
Auth/Firestore/Storage도 필요하므로 전체 emulator 실행 명령을 사용한다.

## Android 테스트와 연결

Android Studio AVD에서는 Android가 host machine의 emulator에 접근할 때 `10.0.2.2`를 사용한다.
아래 Gradle 명령은 repository root에서 실행한다.

```bash
./scripts/ci/run-firebase-emulator-local.sh
```

Emulator Suite 시작, functions lint/build, Android integration test, emulator 종료까지 한 번에 실행하려면 아래 script를 사용한다.

```bash
./scripts/ci/run-firebase-emulator-integration-local.sh
```

실제 디바이스에서는 host machine port를 `adb reverse`로 연결하고 host를 `127.0.0.1`로 고정한다.

```bash
adb reverse tcp:9099 tcp:9099
adb reverse tcp:8080 tcp:8080
adb reverse tcp:5001 tcp:5001
adb reverse tcp:9199 tcp:9199
FIREBASE_EMULATOR_HOST=127.0.0.1 ./scripts/ci/run-firebase-emulator-local.sh
```

위 명령은 Android `:data` 테스트가 실제 Firebase SDK를 통해 server rules/functions 계약을 검증하는 경로다.
`firebaseEmulatorEnabled=true`를 넘기지 않으면 Android emulator integration test는 skip되어 일반 connected
테스트 루프를 방해하지 않는다.

script가 실제로 실행하는 Gradle 명령은 아래와 같다.

```bash
./gradlew :data:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=kr.co.data.firebase.FirebaseEmulatorIntegrationSmokeTest,kr.co.data.feature.diary.sync.FirestoreDiarySyncManagerEmulatorTest,kr.co.data.feature.profile.sync.FirestoreUserProfileSyncManagerEmulatorTest,kr.co.data.feature.setting.sync.FirestoreUserSettingsSyncManagerEmulatorTest \
  -Pandroid.testInstrumentationRunnerArguments.firebaseEmulatorEnabled=true \
  -Pandroid.testInstrumentationRunnerArguments.firebaseEmulatorHost=10.0.2.2
```

## Rules 검증 기준

Firestore Rules 변경 시 최소한 아래 계약을 확인한다.

- 인증되지 않은 사용자는 모든 `/users/{uid}/...` 경로에 접근할 수 없다.
- 인증된 사용자는 자기 `uid`의 profile/settings/diaries만 읽고 쓸 수 있다.
- 다른 사용자의 `uid` 경로에는 읽기/쓰기가 모두 거부된다.
- `/system/{document=**}` 경로는 client read/write가 모두 거부된다.
- profile/settings 문서는 create/read/update만 허용되고 delete는 거부된다.
- diary 문서는 owner 기준 read/write가 허용된다.
- 새 query shape가 생기면 `firestore.indexes.json`과 Android data query를 함께 검증한다.

Storage Rules 변경 시 최소한 아래 계약을 확인한다.

- 인증되지 않은 사용자는 profile photo를 읽거나 쓸 수 없다.
- 인증된 사용자는 `profile_photos/{uid}.jpg` 형식의 자기 파일만 쓸 수 있다.
- 다른 사용자의 파일명으로 쓰는 요청은 거부된다.
- 업로드 크기는 5MB 미만이어야 한다.
- `image/*`가 아닌 content type은 거부된다.
- 읽기는 인증된 사용자에게만 허용된다.

현재 rules 전용 JS/TS 테스트 러너는 없다. rules 검증은 Android `:data` emulator integration test와
Firebase Emulator UI/수동 smoke를 중심으로 수행한다. rules 분기가 늘어나면 `@firebase/rules-unit-testing`
기반의 서버 전용 rules test를 추가하는 것이 좋다.

## Functions 검증 기준

Cloud Functions 변경 시 최소한 아래 계약을 확인한다.

- 모든 callable function은 `asia-northeast3` region을 유지한다.
- timeout은 작업 성격에 맞게 명시한다. 대량 삭제/정리는 충분한 timeout과 batch/pagination을 함께 확인한다.
- callable payload는 서버에서 다시 검증한다. client validation만 믿지 않는다.
- `assertServiceAvailable()` gate를 통과해야 하는 function은 `system/service_control` 상태에 따라 정상/차단 분기를 확인한다.
- account 생성/삭제 function은 Auth, Firestore, Storage 정리 범위를 함께 확인한다.
- `onProfilePhotoUploaded` trigger는 profile photo URL/metadata 업데이트가 재귀나 과도한 write를 만들지 않는지 확인한다.
- `handleBudgetAlert`와 service control function은 비용 방어 상태 변경과 복구 명령을 함께 확인한다.
- App Check 정책을 바꾸면 Android debug/release provider와 emulator 실행 조건을 함께 확인한다.

현재 functions에는 lint/build 검증이 있으며, callable 동작 일부는 Android `:data` emulator integration test에서
검증한다. pure helper가 늘어나면 `firebase-functions-test` 또는 일반 TypeScript unit test로 payload validation,
service control decision, pagination 계산을 분리해 검증한다.

## 테스트 데이터 정책

- 실제 이메일, 일기 본문, 전화번호, 주소, credential, secret을 테스트 데이터나 로그에 남기지 않는다.
- 테스트 식별자는 `uid-test`, `diary-id-test`, `title-test`, `user-...@example.test`처럼 synthetic 값을 사용한다.
- Android emulator integration test는 각 테스트가 필요한 데이터만 직접 seed한다.
- 공용 대형 baseline에 의존하는 테스트는 만들지 않는다. 테스트 간 순서 의존이 생기기 쉽기 때문이다.
- 반복되는 seed 코드가 늘어나면 Android `:data` 쪽에 `FirebaseEmulatorSeeds` 같은 작은 helper를 추가한다.

## emulator-data 사용 기준

`emulator-data/`는 Auth/Firestore/Storage emulator 상태를 import/export하는 선택적 baseline으로만 사용한다.

사용해도 좋은 경우:

- 수동 QA나 demo를 위해 동일한 emulator 초기 상태가 필요할 때.
- Emulator UI에서 rules/functions 동작을 반복 확인할 때.
- app-level manual E2E처럼 큰 초기 상태가 필요한 경우.

기본 자동 테스트에서 피해야 하는 경우:

- 특정 테스트가 baseline의 숨은 문서에 의존하는 경우.
- 테스트 실행 순서에 따라 데이터가 누적되는 경우.
- production-like PII나 실제 credential이 export에 섞일 가능성이 있는 경우.

baseline이 필요할 때만 명시적으로 import한다.

```bash
cd firebase-server
firebase emulators:start --only auth,firestore,storage --import=./emulator-data
```

새 baseline을 갱신해야 할 때는 민감 데이터가 없는지 확인한 뒤 export한다.

```bash
cd firebase-server
firebase emulators:export ./emulator-data --force
```

## 변경별 최소 검증

Firestore Rules 변경:

```bash
cd firebase-server
firebase emulators:start --only auth,firestore
```

그 다음 Android `:data` emulator integration test 중 Firestore 관련 class를 실행한다.

Storage Rules 변경:

```bash
cd firebase-server
firebase emulators:start --only auth,storage
```

그 다음 Storage profile photo smoke를 포함한 Android `:data` emulator integration test를 실행한다.

Functions 변경:

```bash
cd firebase-server
npm --prefix functions run lint
npm --prefix functions run build
firebase emulators:start --only auth,firestore,functions,storage,pubsub --project minary-2c818
```

경로 계약 변경:

```bash
rg "users|diaries|settings|profile|profile_photos|service_control" firebase-server ../core/firebase ../data
```

secret/PII 확인:

```bash
rg "AIza|credential|secret|private_key|client_email" firebase-server/functions firebase-server/*.json firebase-server/*.rules -g "!functions/node_modules/**"
rg "printStackTrace|console\\.log|logger\\.log" firebase-server/functions/src
```

## CI/CD 운영 기준

- PR 기본 검증은 functions lint/build와 Android source compile을 우선한다.
- Firebase Emulator integration test는 manual 또는 nightly job으로 분리한다.
- Emulator job은 emulator start, Android test 실행, emulator log/artifact 보존, cleanup을 명시해야 한다.
- production Firebase project를 대상으로 CI 테스트를 실행하지 않는다.
- App Check, rules, functions, Android provider 경로가 함께 바뀌는 PR은 Android `:data` emulator integration test를 요구한다.

## 추가로 작성하면 좋은 항목

현재 문서는 runbook이다. Firebase 서버 테스트가 더 커지면 아래 항목을 별도 파일이나 하위 섹션으로 추가한다.

- `functions/test/`: payload validation, service control decision, pagination/batch 계산 unit test.
- `rules/test/`: `@firebase/rules-unit-testing` 기반 Firestore/Storage rules matrix.
- `scripts/seed-emulator.*`: 수동 QA용 synthetic baseline 생성 script.
- `scripts/clear-emulator.*`: Auth/Firestore/Storage test data cleanup script.
- CI workflow 문서: emulator job timeout, retry, logcat/emulator log artifact, AVD/physical device host 차이.
- server/client 계약 표: Firebase path, Android provider 메서드, rules match, functions 사용처를 한눈에 보는 표.
