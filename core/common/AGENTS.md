# core/common/AGENTS.md

> **BLUF**
> `:core:common`은 모든 계층에서 공유 가능한 pure Kotlin 공통 모듈이다. Android, UI, DB, Firebase 구현 의존성을 들이지 않는다.

## 역할
- 전 계층에서 의미가 동일한 공통 타입과 상태를 제공한다.
- Android 비의존 순수 Kotlin extension을 제공한다.
- domain/data/presentation이 공유해도 계층 오염이 없는 코드를 소유한다.

## 의존성 규칙
- Kotlin 표준 라이브러리 중심의 pure Kotlin 의존성만 허용한다.
- Android SDK, Compose, Room, Firebase, DataStore, WorkManager, Hilt Android API에 의존하지 않는다.
- 다른 project module에 대한 의존성을 추가하지 않는 것을 기본값으로 한다.

## 패키지/코드 배치 규칙
- 공통 error, state, enum, value object를 둔다.
- 확장 함수는 Android API 없이 동작해야 한다.
- UI 표시, local persistence, remote transport 의미가 섞이면 해당 전용 모듈로 보낸다.
- 공통 타입은 이름만 보고도 특정 계층 소유가 드러나지 않아야 한다.

## 금지사항
- resource id, color, parceler, Context 의존 코드를 두지 않는다.
- Entity, Dto, UiModel처럼 특정 계층 모델을 두지 않는다.
- Firebase path, Room schema, DataStore schema를 두지 않는다.
- logging 외에 Android runtime이 필요한 helper를 추가하지 않는다.

## 변경 시 체크리스트
- 공통 타입 변경 시 domain/data/presentation import와 mapper 영향을 확인한다.
- error/state 변경 시 data error mapping과 presentation error handling을 함께 검증한다.
- Android 의존성이 필요해지면 `:core:common`이 아니라 다른 모듈 책임으로 이동한다.

## 권장 검증
- `./gradlew :core:common:test`
- 의존성 경계 확인: `rg "android\\.|androidx\\.|Firebase|Room|DataStore|WorkManager|Composable" core/common/src/main/java`
- 공통 타입 변경 시 `./gradlew :domain:test :data:compileDebugKotlin :presentation:compileDebugKotlin`
