# :app E2E 테스트 가이드

## 목적

`:app` 테스트는 최종 Android 앱 조립이 올바르게 되었는지 검증한다. 여기에는 Hilt 그래프,
`MainActivity`, Compose UI, 앱 시작 목적지, 네비게이션 콜백, 그리고 feature 간 흐름이 포함된다.
세부 ViewModel 로직은 `:presentation`, UseCase 정책은 `:domain`, repository/Room/DataStore/Firebase
동작은 `:data`와 `:core`의 책임이다.

## 기본 전략

- 기본 PR 경로에는 fake backend 기반 E2E 테스트를 사용한다.
- 실제 `MainActivity`, 실제 Compose 화면, 실제 Navigation, 실제 Hilt ViewModel, 실제 domain UseCase를 유지한다.
- data/service 바인딩은 `@TestInstallIn`을 통해 `FakeAppBackend`로 대체한다.
- Firebase Emulator 테스트는 수동 실행 또는 nightly smoke 체크에만 사용한다.
- Retrofit/OkHttp REST source가 생기기 전에는 MockWebServer를 추가하지 않는다.

## 테스트 위치

- E2E 테스트는 `app/src/androidTest/java/kr/co/minary/e2e`에 둔다.
- 공용 테스트 인프라는 `app/src/androidTest/java/kr/co/minary/testing`에 둔다.
- selector는 가능한 한 리소스 텍스트와 content description을 우선 사용한다.
- 노드가 불안정하거나 모호할 때만 `testTag`를 추가한다.

## `:app` E2E에 포함되는 것

- 앱 실행: 로그인 안 됨, 로그인 됨, 유지보수 모드.
- 계정 골든 경로: welcome -> sign in -> home, sign up, sign out.
- feature 간 네비게이션: 하단 탭, calendar/search에서 diary detail 이동, settings에서 profile 이동.
- diary 골든 경로: 생성, 수정, 삭제, 저장 실패 시 현재 화면 유지.
- sync/status 화면: 초기 sync 진행, 실패 후 재시도, 완료 상태.

## `:app` E2E에 포함되지 않는 것

- Compose 렌더링이 필요 없는 ViewModel 상태 전이.
- mapper, formatter, validation 전용 테스트.
- repository 정책, Room query, DataStore persistence, Firebase SDK 계약.
- 화면 레이아웃을 과도하게 세부 검증하는 테스트.

## 실행 명령

빠른 PR 검증은 디바이스를 띄우지 않고 app E2E 컴파일까지 확인한다.

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugAndroidTestKotlin
```

수동 또는 nightly 검증은 실제 connected E2E를 실행한다.

```bash
./gradlew :app:connectedDebugAndroidTest
```

모든 app E2E 클래스에는 `androidx.test.filters.LargeTest`를 붙인다. CI 러너에서 app E2E만
선택해야 하면 다음 instrumentation 인자를 전달한다.

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.annotation=androidx.test.filters.LargeTest
```

`connectedDebugAndroidTest`는 연결된 모든 타깃에서 실행된다. 로컬 검증에서는 이를 의도적으로
사용한다. 장치별 Compose, 네비게이션, Activity launch 문제를 잡고 싶을 때는 실제 디바이스와
Android Studio AVD를 함께 연결해 두는 것이 좋다. 수동 실행 전에는 `adb devices`로 대상 목록을
확인한다.

Firebase Emulator E2E는 기본 PR 검증과 분리한다. Android Studio AVD에서는 `10.0.2.2`를 사용하고,
실제 디바이스에서는 `adb reverse` 또는 명시적인 `firebaseEmulatorHost=127.0.0.1` 인자를 사용한다.

## CI/CD 정책

- `:app:connectedDebugAndroidTest`를 모든 PR에 기본으로 실행하지 않는다. 느리고 안정적인 Android runtime에
  의존한다.
- PR 기본 검증은 `:app:testDebugUnitTest`와 `:app:compileDebugAndroidTestKotlin`이다.
- E2E에 민감한 변경은 머지 전에 수동으로 `:app:connectedDebugAndroidTest`를 실행하거나, 고정된 emulator image를
  쓰는 nightly/manual CI job에서 실행한다.
- Firebase Emulator E2E는 fake backend app E2E 경로와 분리한다. emulator 검증은 별도 명령, 명시적 emulator 시작,
  별도 정리를 가진다.
- 이 브랜치에서는 GitHub Actions workflow 파일을 추가하지 않는다. 테스트 전략 변경과 CI platform wiring은
  별도 CI 브랜치에서 반영한다.

## 안정성 규칙

- app E2E는 fake backend 상태를 사용한다. 기본 app E2E 테스트에서 Firebase, Room, DataStore, network, wall clock
  동작에 의존하지 않는다.
- `launchApp()` 전에 fake state를 준비한다. `BaseE2ETest`는 각 테스트 전후로 fake state를 초기화한다.
- 고정 sleep은 사용하지 않는다. Compose 동기화, `waitForIdle()`, bounded `waitUntil`을 우선한다.
- selector는 resource text와 content description을 우선한다. `testTag`는 모호한 interactive node에만 사용한다.
- assertion은 integration boundary에 둔다. 목적지가 바뀌었는지, 핵심 콘텐츠가 보이는지, fake backend 상태가 바뀌었는지만
  확인한다. 이곳에서 상세 ViewModel/repository 테스트를 중복 작성하지 않는다.
- 테스트 순서에 의존하지 않는다. 각 E2E 테스트는 자기 session/diary/maintenance/failure 상태를 직접 준비해야 한다.
- AVD에서는 통과하지만 실제 디바이스에서 실패하는 테스트는 중요한 증거로 본다. 보통 lifecycle, viewport, keyboard,
  threading 가정이 드러난다.

## 리포트와 산출물

connected 실행 후에는 아래 경로를 확인하거나 업로드한다.

```text
app/build/reports/androidTests/connected/debug
app/build/outputs/androidTest-results/connected
```

향후 CI workflow job은 실패 시 emulator logcat과 screenshot도 보존해야 한다. 이런 산출물은 production 코드나 test source
가 아니라 CI workflow 계층에서 관리한다.
