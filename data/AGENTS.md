# data/AGENTS.md

> **BLUF**
> Room(관계형/일기)과 Proto DataStore(설정)를 SSOT로 관리하며, Firebase 연동 및 WorkManager 백그라운드 동기화를 수행하는 데이터 구현 계층이다.

## 1. SSOT (Offline-first) 및 구현 규칙
- **SSOT**: 모든 데이터는 Local DB(Room/DataStore)를 경유한다. UI는 오직 로컬의 `Flow`만 구독한다.
- **Provider & Paths**: Firebase 인스턴스에 직접 접근하지 않고 `FirebaseFirestoreProvider` 등 Provider 클래스를 통해 캡슐화한다. 경로 수정 시 서버 측 `rules` 및 `functions`와의 동기화가 필수적이다.
- **DataStore**: `MinaryPrefsDataStore` 등 Protobuf 기반 설정을 관리하며, 스키마 변경 시 세심한 주의를 요한다.
- **모델 명명**: 로컬 DB 테이블은 **`Entity`**, 서버 통신 모델은 **`Dto`** 접미사를 엄격히 구분하여 사용한다.
- **Room 규칙**: DAO는 Flow 반환을 우선한다 (`observe(): Flow<List<Entity>>`). Transaction이 필요한 경우 `@Transaction`을 사용하며, UI에서 DAO 직접 접근을 금지한다.

## 2. 동기화 및 비용 최적화 (LWW & Chunked)
- **SyncManager**: 동기화 로직은 RepositoryImpl 내부에 직접 작성하지 않고 `Repository -> SyncManager -> DataSource` 구조를 유지한다. Sync 과정은 Upload, Download, Conflict Resolution 3단계로 분리한다.
- **LWW**: `lastModifiedAt`을 비교하여 최신 데이터를 덮어쓴다. 로컬이 최신이면 로컬 데이터로, 원격이 최신이면 원격 데이터로 업데이트 한다.
- **Cost**: Realtime Listener는 최소화하고, 필요한 시점에만 `get()`을 통해 Fetch 한다. `CHUNK_SIZE = 50` 정책을 준수한다.
- **Worker**: Worker는 Domain 계약(`SyncManager`)에 작업을 위임하는 껍데기 역할만 수행한다. 주요 역할은 **1. 실행 조건 확인, 2. Domain 계약 호출, 3. 결과 반환**으로 한정하며 비즈니스 로직을 포함하지 않는다.

## 3. DI 및 Coroutine 규칙
- **DI 모듈 분리**: Domain 계층의 계약(Interface)과 이를 수행하는 Data 계층의 구현체 간 의존성 바인딩은 반드시 `data/di/module/feature/` 내에 분리된 Hilt 모듈로 작성한다.
- **인프라 설정**: DB, Network, RemoteConfig 등 공통 인프라 설정은 `data/di/module/infra/`에서 관리한다.
- **Qualifier 사용**: 동일한 타입의 의존성(예: Coroutine Dispatcher) 주입 시 `data/di/qualifier/`에 정의된 한정자를 반드시 사용한다.
- **Coroutine**: Dispatcher 직접 사용을 금지한다. 반드시 Hilt로 주입받은 `@IoDispatcher`, `@DefaultDispatcher`를 사용하여 테스트 가능성을 유지한다.

## 4. Mapper 및 에러 매핑
- **Mapper**: `Entity` <-> `Domain`, `Dto` <-> `Domain` 변환을 위한 명시적 Mapper(`DiaryMapper` 등)를 반드시 사용한다. 모델 수정 시 Mapper를 즉시 업데이트한다.
- **Error Mapping**: Firebase/Room의 모든 외부 예외는 `ExceptionMapper.kt`를 통해 `DomainError`로 변환한다. (`runSuspendCatching` 사용 권장)