# data/AGENTS.md

> **BLUF**
> `:data`는 domain contract의 구현 계층이다. Local SSOT, Firebase 연동, sync orchestration을 담당하되 infra provider는 `core` 모듈을 사용한다.

## 역할
- RepositoryImpl, DataSource, Mapper, SyncManager/Worker, domain contract binding을 소유한다.
- Room/DataStore 기반 local source를 SSOT로 사용하고 Firebase와 동기화한다.
- 외부 예외를 `DomainError` 또는 `AppResult<Value>` 흐름으로 변환해 domain/presentation 경계를 안정화한다.
- Firebase read/write 비용과 offline 동작을 동시에 보호한다.

## 의존성 규칙
- `:domain`과 필요한 `:core:*`에 의존한다.
- Room 구성요소는 `:core:database`, Proto DataStore 구성요소는 `:core:datastore`, Firebase provider는 `:core:firebase`, local storage provider는 `:core:storage`, 공통 DI는 `:core:di`를 사용한다.
- Firebase SDK 인스턴스는 직접 주입하지 않는다. Auth/Firestore/Functions/Storage/RemoteConfig 접근은 `:core:firebase` provider를 통해 수행한다.
- `:presentation`과 `:app`을 참조하지 않는다.
- domain contract에 구현 기술 API가 새어 나가지 않게 한다.

## 패키지/코드 배치 규칙
- feature 구현은 `feature/{name}` 하위에 둔다.
- feature 내부는 필요에 따라 `repository`, `source`, `mapper`, `model`, `sync`로 구분한다.
- 공통 service 구현은 `service/*`에 둔다.
- DI는 Constructor Injection을 우선하고, `@Binds`, `@Provides`는 필요한 경우에만 사용하고, feature/service 성격에 맞춰 분리한다.
- local model은 `Entity`, remote model은 `Dto` 접미사를 사용한다.
- `Entity <-> Domain`, `Dto <-> Domain` 변환은 명시적 mapper로 처리한다.
- Firebase SDK는 provider를 통해 접근하고, 경로 문자열 변경 시 `:core:firebase`와 `firebase-server`를 함께 갱신한다.
- Firebase 예외 타입, listener registration, snapshot처럼 data 구현에 필요한 SDK value/type 사용은 허용하되, SDK singleton 인스턴스 접근은 provider로 제한한다.
- Firebase/Room/DataStore/IO 실패는 data 내부 mapper에서 `DomainError`로 변환하고, raw exception을 domain/presentation으로 노출하지 않는다.
- 이미 의미를 아는 내부 실패는 예외를 던지지 말고 `Err(DomainError.*)`로 반환한다.

## 금지사항
- UI 상태, Compose, ViewModel, navigation을 참조하지 않는다.
- `FirebaseAuth`, `FirebaseFirestore`, `FirebaseFunctions`, `FirebaseStorage`, `FirebaseRemoteConfig`를 생성자에 직접 주입하지 않는다.
- RepositoryImpl에 복잡한 sync 정책을 직접 누적하지 않는다. sync 책임은 별도 sync 구성요소로 분리한다.
- Firestore realtime listener와 remote read 범위를 비용 고려 없이 넓히지 않는다.
- Coroutine Dispatcher를 직접 고정하지 않는다. `:core:di` qualifier로 주입받은 dispatcher/scope를 사용한다.
- 외부 예외 매핑 코드를 `:core:common`으로 올리지 않는다. 구현 기술 예외 변환은 data 책임으로 유지한다.
- PII를 로그에 남기지 않는다.

## 변경 시 체크리스트
- local schema나 DAO 영향 변경 시 mapper, migration, query 비용을 확인한다.
- remote 경로나 payload 변경 시 `:core:firebase`와 `firebase-server`를 함께 검증한다.
- sync 정책 변경 시 LWW 기준 시간, Worker 실행 조건, retry/idempotency를 확인한다.
- diary chunk sync 변경 시 `CHUNK_SIZE` 기준과 Upload/Download/Conflict Resolution 흐름을 함께 검증한다.
- 새 binding 추가 시 domain contract와 data 구현체의 방향이 맞는지 확인한다.
- Firebase 호출 추가 시 read/write 횟수, listener lifecycle, offline fallback을 검증한다.

## 권장 검증
- `./gradlew :data:test`
- `./gradlew :data:compileDebugKotlin`
- import 경계 확인: `rg "kr\\.co\\.presentation|androidx\\.compose" data/src/main/java`
- Firebase SDK 직접 주입 확인: `rg ": Firebase(Auth|Firestore|Functions|Storage|RemoteConfig)[,)]" data/src/main/java -g "*.kt"`
- Firebase 경로 변경 시 `firebase-server` rules/functions와 함께 smoke check한다.
