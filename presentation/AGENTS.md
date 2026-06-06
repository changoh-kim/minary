# presentation/AGENTS.md

> **BLUF**
> Jetpack Compose와 Orbit MVI를 결합한 순수 UI 계층. ViewModel은 오직 `UseCase`만 호출하며, UI에서 데이터를 직접 가공하거나 원격 데이터를 직접 조회하는 행위를 금지한다.

## 1. Orbit MVI 및 네비게이션 규칙
- **MVI 구현**: ViewModel은 반드시 `ContainerHost<State, SideEffect>`를 구현한다. 상태 변경은 `intent { reduce { ... } }` 내에서만 수행하며, SideEffect는 `postSideEffect()`를 사용한다. State 외부 직접 수정을 금지한다.
- **State**: 화면 렌더링에 필요한 상태. 오직 `intent { reduce { ... } }` 내에서만 변경.
- **SideEffect**: Navigation, Toast 등 단발성 이벤트. `postSideEffect()`로 처리.
- **Compose 수집**: Flow 수집 시 `collectAsStateWithLifecycle()`을 사용하여 Lifecycle 안전성을 보장한다.
- **Navigation**: 개별 기능(Feature) 내 화면 이동은 `feature/{name}/navigation/`에 위치한 **전용 NavGraph 파일(예: {name}Graph.kt)**을 통해 관리한다. 앱 수준의 글로벌 이동 및 컨트롤은 반드시 **`MinaryAppState`**를 통해서만 수행하며, 신규 기능 추가 시 `MinaryRoutes.kt`에 Route를 정의하고 `MinaryNavHost`에 해당 NavGraph를 등록한다.
- **Initialization**: 앱 진입 시 `MainActivityViewModel`에서 Session, ServerTime, Service Blocked 상태를 확인하여 초기 UI 상태를 결정한다.
- **모델 명명**: UI 상태 표현을 위한 모델은 명확히 **`UiModel`** 접미사를 사용한다 (예: `UserUiModel`).

## 2. UI 구조 및 Preview 규칙
- **Feature 패키지 구조**: 새 UI 생성 시 아래 구조를 엄격히 따른다.
    `feature/{name}` (composable, design, mapper, model, navigation, preview, screen, viewmodel)
- **Feature Previews**: 기능(Feature) 내 모든 화면을 한눈에 검토할 수 있도록 `feature/{name}/design`에 **`{name}Previews.kt`**를 작성하고, 각 Screen의 Preview를 반드시 이곳에 등록한다.
- **Screen & Preview**: 
    - 최상위 화면 Composable은 반드시 **`Screen`** 접미사를 사용한다 (예: `HomeScreen`).
    - 각 `Screen`은 전용 Preview를 포함해야 하며, 다양한 상태나 복잡한 데이터가 필요한 경우 `feature/{name}/preview` 패키지에 **`{name}ScreenPreviewParameterProvider`**를 구현하여 데이터를 분리 관리한다.
- **UDF**: 모든 Composable은 비즈니스 로직이 없는 순수 함수로 작성하며 상태 호이스팅을 준수한다.
- **Theme**: `MinaryTheme`을 기반으로 하며, 커스텀 속성은 `MaterialTheme` 확장을 통해 접근한다.

## 3. 에러 처리 및 Mapper 규칙
- **Error**: UseCase 실패 시 `DomainError`를 `handleDomainError` 확장 함수를 통해 `UiText`로 변환하여 노출한다.
- **Mapper**: `Domain` 모델과 `UiModel` 간의 변환을 담당하는 Mapper를 반드시 구성하며, 모델 수정 시 Mapper를 즉시 업데이트한다.
- **Strings**: UI에 하드코딩된 문자열을 쓰지 않는다. `strings.xml` 기반의 `UiText.StringResource`를 사용한다. (ko/en 지원 필수)
- **PII**: Log 및 UI 에러 메시지에 민감 정보(이메일, 일기 내용 등) 노출 절대 금지.
