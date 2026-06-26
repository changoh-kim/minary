# presentation/AGENTS.md

> **BLUF**
> `:presentation`은 Jetpack Compose + Orbit MVI 기반 UI 계층이다. 화면은 Domain UseCase만 호출하고 data 구현체나 원격 SDK를 직접 참조하지 않는다.

## 역할
- Compose 화면, UI state, user action, side effect, navigation graph를 소유한다.
- Domain model을 화면에 맞는 `UiModel`로 변환한다.
- 앱 UI shell은 `presentation/app`에 두되, Gradle `:app` 모듈과 혼동하지 않는다.
- feature 모듈화에 대비해 feature 간 내부 구현 공유를 최소화한다.

## 의존성 규칙
- `:domain`, `:core:common`, `:core:ui:common`, `:core:ui:design`에 의존한다.
- `:data`, `:app`, Firebase SDK, DAO, DataSource를 직접 참조하지 않는다.
- 공통 UI 상태/텍스트/에러 처리는 `:core:ui:common`, theme/component/token은 `:core:ui:design`을 사용한다.
- Android framework entry point나 application 초기화 책임은 Gradle `:app` 또는 app shell 책임과 혼동하지 않는다.
- ViewModel은 UseCase만 호출하고, 상태 변경은 Orbit `intent/reduce` 흐름 안에서 처리한다.
- UI는 local source 기반 domain flow를 관찰하는 UseCase 결과만 사용하고, remote를 직접 조회하지 않는다.
- UseCase 실패는 ViewModel에서 화면 문맥에 맞는 `when (DomainError)` 분기로 처리한다.

## 패키지/코드 배치 규칙
- app-level UI shell과 navigation 조립은 `app` 패키지에 둔다.
- 기능 화면은 `feature/{name}` 하위에 두고, feature 내부에서 `model`, `mapper`, `navigation`, `screen`, `preview` 역할을 구분한다.
- 화면은 `screen/{screenName}` 단위로 묶고, 화면 전용 component는 해당 화면 하위에 둔다.
- UI 모델은 `UiModel` 접미사를 사용하고, mapper는 UI 변환 책임이 드러나는 이름을 사용한다.
- 여러 feature가 presentation model을 공유하지 않는다. 화면/기능별 `UiModel`을 우선한다.
- `Screen(viewModel = ...)`과 state 기반 `Content`를 분리해 preview/testability를 유지한다.
- 화면 로딩 상태를 나타낼때 `:core:ui:common` load의 파일과, `:core:ui:design`의 skeleton/loading component를 우선 사용한다.
- 에러 메시지는 화면별 side effect에서 `UiText.StringResource`를 우선 사용한다.

## 금지사항
- ViewModel에서 RepositoryImpl, DAO, DataSource, Firebase SDK를 직접 호출하지 않는다.
- Composable에 비즈니스 로직이나 동기화 정책을 넣지 않는다.
- feature 화면이 다른 feature의 내부 화면/component를 직접 참조하지 않는다.
- 문자열, 사용자 메시지, 에러 문구를 하드코딩하지 않는다.
- PII를 로그, snackbar, 에러 메시지에 노출하지 않는다.
- `DomainError.Unexpected` 처리 시 raw cause나 `Throwable`을 로그, 상태, side effect로 노출하지 않는다.
- State를 `reduce` 밖에서 직접 변경하지 않는다. Navigation, snackbar, toast는 SideEffect로 처리한다.

## 변경 시 체크리스트
- 새 화면 추가 시 State/Action/SideEffect/ViewModel/Screen/Content 책임을 분리한다.
- route 추가 시 app-level navigation 조립과 feature graph 연결을 함께 검증한다.
- domain model 변경 시 feature-local UiModel과 mapper를 함께 갱신한다.
- 공통 component를 만들 때 design system인지 UI support인지 먼저 구분한다.
- UI 변경 시 Compose preview와 실제 화면 상태를 함께 검증한다.
- UseCase 실패 처리가 바뀌면 화면별 `DomainError` 분기와 ko/en string resource를 함께 갱신한다.

## 권장 검증
- `./gradlew :presentation:compileDebugKotlin`
- import 경계 확인: `rg "kr\\.co\\.data|com\\.google\\.firebase|kr\\.co\\.minary" presentation/src/main/java`
- 화면 구조 확인: `find presentation/src/main/java/kr/co/presentation/feature -maxdepth 4 -type d | sort`
- UI root 변경 시 `./gradlew :app:assembleDebug`
