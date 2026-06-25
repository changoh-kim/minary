# core/database/AGENTS.md

> **BLUF**
> `:core:database`는 Room 기반 로컬 DB 구성요소 전용 모듈이다. data access의 기반만 제공하고 repository나 sync 정책은 소유하지 않는다.

## 역할
- Room database, DAO, Entity, converter, relation model, database provider를 소유한다.
- 로컬 스키마와 query contract를 안정적으로 제공한다.
- data 모듈이 local source를 구현할 수 있는 persistence 기반을 제공한다.

## 의존성 규칙
- Room과 Android persistence에 필요한 의존성만 허용한다.
- `:domain`, `:data`, `:presentation`을 참조하지 않는다.
- Firestore/Storage/Functions, DataStore, Network, UI 의존성을 추가하지 않는다.
- 사용자별 로컬 DB provider에 필요한 `FirebaseAuth` 사용은 허용하되, 인증 정책이나 remote 접근 로직을 포함하지 않는다.
- `:core:common` 타입은 DB schema 의미를 오염시키지 않을 때만 사용한다.

## 패키지/코드 배치 규칙
- DB schema와 직접 관련된 타입만 둔다.
- DAO는 local persistence contract로 유지하고 business rule을 포함하지 않는다.
- query는 Flow 기반 관찰을 우선하되, 일회성 조회는 의도가 드러나게 분리한다.
- Entity는 persistence 구조를 표현하고 domain 정책을 포함하지 않는다.

## 금지사항
- RepositoryImpl, DataSource, SyncManager, Worker를 두지 않는다.
- domain usecase를 호출하지 않는다.
- UI model이나 presentation state를 두지 않는다.
- remote sync 정책을 DAO나 Entity에 섞지 않는다.

## 변경 시 체크리스트
- Entity/DAO 변경 시 migration 필요성과 data mapper 영향을 확인한다.
- relation 조회 변경 시 transaction 필요성을 확인한다.
- query 변경 시 성능, index, Flow invalidation 영향을 검증한다.
- schema 변경이 사용자 데이터 손실을 만들지 않는지 migration 관점에서 검증한다.

## 권장 검증
- `./gradlew :core:database:compileDebugKotlin`
- 의존성 경계 확인: `rg "kr\\.co\\.domain|kr\\.co\\.data|kr\\.co\\.presentation|FirebaseFirestore|FirebaseStorage|FirebaseFunctions|DataStore|Composable" core/database/src/main/java`
- DAO/Entity 변경 시 data compile: `./gradlew :data:compileDebugKotlin`
