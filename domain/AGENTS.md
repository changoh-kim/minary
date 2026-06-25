# domain/AGENTS.md

> **BLUF**
> `:domain`은 Minary의 순수 비즈니스 계약과 정책을 담는 모듈이다. 구현 기술을 모르고 Android framework에 의존하지 않는다.

## 역할
- domain model, repository contract, usecase, sync/service contract를 정의한다.
- Offline-first와 LWW 동기화에 필요한 비즈니스 규칙의 경계를 제공한다.
- data와 presentation이 공유하는 의미를 순수 Kotlin 계약으로 표현한다.

## 의존성 규칙
- `:core:common`과 순수 Kotlin 의존성만 허용한다.
- `javax.inject`처럼 순수 DI annotation은 허용하되 Android/Hilt Android API는 사용하지 않는다.
- 새 라이브러리가 필요하면 Android 비의존 라이브러리만 선택한다.
- Android SDK, Compose, Firebase SDK, Room, DataStore, WorkManager 구현체를 직접 참조하지 않는다.
- 공통 error/state/model은 `:core:common`을 사용한다.

## 패키지/코드 배치 규칙
- 기능별 계약은 `feature/{name}` 하위에 둔다.
- 기능 내부는 필요에 따라 `model`, `repository`, `usecase`, `sync`로 구분한다.
- 외부 시스템이나 앱 전역 서비스 성격의 계약은 `service/*`에 둔다.
- domain model은 순수 명칭을 사용하고 `UiModel`, `Entity`, `Dto` 접미사를 붙이지 않는다.
- UseCase는 단일 의도를 표현하고 상태를 저장하지 않는 호출형 API를 우선한다.
- 계약 성격은 `repository`, `sync`, `service`로 구분하고 `port` 패키지는 사용하지 않는다.
- 복잡한 순수 비즈니스 계산은 Repository가 아니라 feature 내부 generator/service 성격의 domain logic으로 분리한다.

## 금지사항
- data/presentation 모델로 변환하는 mapper를 domain에 두지 않는다.
- Firebase, Room, WorkManager 같은 구현 기술명을 domain contract에 노출하지 않는다.
- 동기화 기준 시간에 기기 시간을 직접 사용하지 않는다. 시간은 domain service 계약을 통해 다룬다.
- UI 문자열, Android resource id, `Context`를 domain에 넣지 않는다.
- raw exception을 domain API로 노출하지 않는다. 실패는 `Result<Value, DomainError>`로 표현한다.

## 변경 시 체크리스트
- domain model이 바뀌면 data mapper와 presentation mapper 영향을 함께 검증한다.
- error/state 계층이 바뀌면 data error mapping과 presentation error handling을 함께 검증한다.
- sync/service contract가 바뀌면 data 구현체와 DI binding을 함께 갱신한다.
- UseCase 추가 시 repository/service contract가 구현 세부사항을 드러내지 않게 유지한다.

## 권장 검증
- `./gradlew :domain:test`
- import 경계 확인: `rg "android\\.|androidx\\.compose|Firebase|Room|WorkManager|DataStore" domain/src/main/java`
- contract 변경 시 `./gradlew :data:compileDebugKotlin :presentation:compileDebugKotlin`
