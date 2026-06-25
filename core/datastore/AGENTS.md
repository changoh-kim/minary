# core/datastore/AGENTS.md

> **BLUF**
> `:core:datastore`는 Proto DataStore provider, serializer, proto schema 전용 모듈이다. 저장소 생성 기반만 제공하고 feature 정책은 소유하지 않는다.

## 역할
- Proto schema와 serializer를 관리한다.
- DataStore provider와 생성에 필요한 최소 설정을 제공한다.
- data 모듈이 settings/session/sync 상태를 local source로 다룰 수 있는 기반을 제공한다.

## 의존성 규칙
- DataStore와 protobuf에 필요한 의존성만 허용한다.
- `:domain`, `:data`, `:presentation`을 참조하지 않는다.
- Firestore/Storage/Functions, Room, UI 의존성을 추가하지 않는다.
- 사용자별 DataStore provider에 필요한 `FirebaseAuth` 사용은 허용하되, 인증 정책이나 remote 접근 로직을 포함하지 않는다.
- domain 의미 변환은 이 모듈이 아니라 data mapper에서 처리한다.

## 패키지/코드 배치 규칙
- proto schema는 backward compatibility를 우선해 관리한다.
- provider/serializer는 DataStore 생성과 복구 책임만 가진다.
- DataStore 파일명/path 변경은 기존 사용자 데이터 호환성을 보장할 때만 허용한다.
- proto field는 추가 중심으로 관리한다.

## 금지사항
- RepositoryImpl, DataSource, UseCase, sync policy를 두지 않는다.
- UI model이나 domain mapper를 두지 않는다.
- proto field number를 재사용하거나 기존 의미를 바꾸지 않는다.
- Firestore, Firebase Storage, Room 접근 코드를 추가하지 않는다.

## 변경 시 체크리스트
- schema 변경 시 field number, default value, backward compatibility를 확인한다.
- serializer 변경 시 corruption handling과 data 사용처를 검증한다.
- DataStore 파일명/path 변경 시 기존 사용자 데이터 호환성을 확인한다.

## 권장 검증
- `./gradlew :core:datastore:compileDebugKotlin`
- proto/schema 변경 시 `./gradlew :data:compileDebugKotlin`
- 의존성 경계 확인: `rg "kr\\.co\\.domain|kr\\.co\\.data|kr\\.co\\.presentation|FirebaseFirestore|FirebaseStorage|FirebaseFunctions|Room|Composable" core/datastore/src/main`
