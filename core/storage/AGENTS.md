# core/storage/AGENTS.md

> **BLUF**
> `:core:storage`는 앱 내부 로컬 파일 저장소 경로와 provider 전용 모듈이다. 파일 처리 정책이나 업로드 구현은 소유하지 않는다.

## 역할
- local storage base path와 provider를 제공한다.
- 사용자별 내부 저장소 접근에 필요한 공통 설정을 제공한다.
- data 모듈이 파일 저장소 구현을 작성할 수 있는 기반을 제공한다.

## 의존성 규칙
- Android local file storage에 필요한 최소 의존성만 허용한다.
- `:domain`, `:data`, `:presentation`을 참조하지 않는다.
- Firebase Storage, image processing, feature policy 의존성을 추가하지 않는다.
- 사용자별 로컬 경로 provider에 필요한 `FirebaseAuth` 사용은 허용하되, 인증 정책이나 remote storage 접근 로직을 포함하지 않는다.
- path provider는 business rule이 아니라 저장소 위치 계산만 담당한다.

## 패키지/코드 배치 규칙
- 경로 계산과 provider 책임만 둔다.
- 파일의 의미, 압축, 변환, 업로드 정책은 data/service 또는 domain service 구현에서 다룬다.
- 경로 문자열 변경은 기존 파일 호환성과 삭제 정책을 보장할 때만 허용한다.

## 금지사항
- 이미지 리사이징, 압축, 포맷 변환 로직을 두지 않는다.
- Firebase Storage 업로드/다운로드 구현을 두지 않는다.
- RepositoryImpl, DataSource, UseCase를 두지 않는다.
- PII가 포함될 수 있는 경로를 로그에 남기지 않는다.

## 변경 시 체크리스트
- 경로 변경 시 기존 파일 호환성, 삭제 정책, data 사용처를 검증한다.
- 저장소 provider 변경 시 Android version별 동작 차이를 검증한다.
- 파일 정책이 필요해지면 `:core:storage`가 아니라 상위 구현 계층으로 분리한다.

## 권장 검증
- `./gradlew :core:storage:compileDebugKotlin`
- 의존성 경계 확인: `rg "kr\\.co\\.domain|kr\\.co\\.data|kr\\.co\\.presentation|FirebaseStorage|Composable" core/storage/src/main/java`
- 경로 변경 시 관련 data 사용처 검색: `rg "UserInternalStorageProvider|LocalStoragePathProvider" data core`
