# domain/AGENTS.md

> **BLUF**
> Minary의 순수 비즈니스 뇌(Brain) 역할을 한다. Android SDK, Firebase SDK, DB 구현체 등을 전혀 모르는 **Pure Kotlin** 모듈이어야 한다.

## 1. 의존성 및 모델링 규칙
- **의존성 금지**: `android.*`, `firebase.*`, `room.*`, `work.*`, `Context` 절대 금지. (예외: `javax.inject`, `androidx.paging.common`)
- **모델 명명**: 비즈니스 본질을 나타내는 **순수 명칭**을 사용한다 (예: `User`, `Diary`). 계층별 접미사(`Model`, `Dto` 등)를 붙이지 않는다.
- **UseCase**: `operator fun invoke()`로 실행하며, 상태를 내부에 저장하지 않는다. 단일 책임 원칙(SRP)을 준수한다.
- **Contract First**: 새로운 기능 추가 시 반드시 `domain`의 모델과 Repository 인터페이스를 먼저 정의한다.

## 2. 에러 (DomainError) 및 시간 규칙
- **Error**: 반드시 `sealed interface DomainError` 계층으로 추상화하며, `Result<Value, DomainError>`를 반환한다.
- **Time**: 기기 시간(`System.currentTimeMillis()`) 사용 금지. 항상 `ServerTimeProvider` 계약을 사용한다.
- **State**: `domain.common.state`의 `SyncProcessState` 등 공통 상태 객체를 적극 활용한다.

## 3. 서비스 및 계약 규칙
- **Domain Service**: 복잡한 비즈니스 정책은 Repository가 아닌 `CalendarGenerator`, `EmotionAnalyzer` 등 Domain Service로 분리한다.
- **Sync 및 인터페이스 계약**: Domain은 데이터의 원천(Firestore, Room 등)이나 상세 구현 방식을 모른다. 비즈니스 로직 수행에 필요한 **추상화된 계약(`SyncManager`, `SyncResult` 등)**만을 정의하며, 외부 기술 라이브러리 API의 직접 참조를 절대 금지한다.
- **Mapper**: 도메인 모델은 외부 계층의 Mapper를 통해서만 변환되며, 도메인 내부에는 매핑 로직을 두지 않는다.