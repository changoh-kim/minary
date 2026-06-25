# Minary - AGENTS.md

> **BLUF**
> Minary는 Room/DataStore를 SSOT(Single Source of Truth)로 사용하는 Offline-first AI 다이어리 앱이다. 네트워크 상태와 무관하게 핵심 기능이 동작해야 하며, Firebase 비용 방어와 LWW(Last-Write-Wins) 동기화가 아키텍처의 핵심이다.

## 작업 원칙
- 모든 응답, 진행 보고, 검증 결과는 한국어를 기본으로 한다. 기술 용어, 코드 식별자, 에러 메시지는 영어 원문을 유지할 수 있다.
- 기존 사용자 변경을 되돌리지 않는다.
- `git reset --hard`, `git checkout --`, 대량 삭제 등 파괴적 명령은 사용자가 명시적으로 요청한 경우에만 실행한다.
- secret, credential, PII, 일기 본문, 이메일 주소를 로그나 문서 예시에 남기지 않는다.
- 기존 주석 중 로직 설명이나 의사결정 근거가 담긴 내용은 삭제하지 않는다.
- 특정 모듈이나 `firebase-server` 하위에서 작업할 때는 해당 경로의 `AGENTS.md`를 반드시 먼저 확인하고, 루트 지침에 더해 적용한다.
- 변경 후에는 작업 성격에 맞는 최소 검증을 실행하고, 실패 또는 미실행 검증을 보고한다.

## 의존성 규칙
- 기본 패턴은 Clean Architecture + Orbit MVI + UDF(단방향 데이터 흐름)이다.
- Gradle 의존성 방향은 아래 흐름을 따른다.
    - `:app` -> `:presentation`, `:data`, `:domain`, `:core:di`
    - `:presentation` -> `:domain`, `:core:common`, `:core:ui:common`, `:core:ui:design`
    - `:data` -> `:domain`, `:core:common`, `:core:di`, `:core:database`, `:core:datastore`, `:core:firebase`, `:core:storage`
    - `:domain` -> `:core:common`
- 역방향 의존성을 만들지 않는다. 특히 `domain`은 `data`, `presentation`, Android 구현을 몰라야 한다.
- `presentation`은 `data`와 `app`을 참조하지 않는다.
- Firebase 경로, rules, functions 계약 변경 시 Android client와 `firebase-server`를 함께 검증한다.

## 패키지/코드 배치 규칙
- 모델 접미사는 계층별로 구분한다.
    - `domain`: 순수 명칭
    - `presentation`: `UiModel`
    - `data`: local은 `Entity`, remote는 `Dto`
- 새 기능은 `domain` 계약 -> `data` 구현 -> `presentation` UI -> `app` 최종 조립 순서로 추가한다.
- 공통 코드는 성격에 맞는 `core` 모듈에만 둔다.
    - `:core:common`: pure Kotlin 공통 타입과 extension
    - `:core:di`: 공통 DI qualifier/module
    - `:core:database`: Room 구성요소
    - `:core:datastore`: Proto DataStore 구성요소
    - `:core:firebase`: Firebase provider와 공통 경로 계약
    - `:core:storage`: local storage provider
    - `:core:ui:common`: UI support
    - `:core:ui:design`: Compose design system
- 기존 패키지 원칙으로 설명 가능한 변경은 새 구조를 만들기보다 현재 구조를 확장한다.

## 금지사항
- 동기화 기준 시간에 기기 시간(`System.currentTimeMillis()`)을 직접 사용하지 않는다. `ServerTimeProvider` 계약을 사용한다.
- 라이브러리 버전을 `build.gradle.kts`나 소스에 하드코딩하지 않는다. 새 의존성은 `gradle/libs.versions.toml`에 등록하고 `libs.*` alias로 참조한다.
- 모듈 고유 책임을 다른 모듈에 편의상 추가하지 않는다. 경계가 애매하면 하위 `AGENTS.md`의 역할 정의를 먼저 확인한다.
- 코드만 보면 알 수 있는 클래스/파일 목록을 AGENTS 문서에 장황하게 추가하지 않는다.

## 변경 시 체크리스트
- Gradle 모듈이 추가/삭제/분리/병합되면 루트 의존성 설명과 하위 `AGENTS.md` 배치를 갱신한다.
- 의존성 방향, 패키지 구조 원칙, MVI/sync/DI/model naming 규칙이 바뀌면 관련 `AGENTS.md`를 함께 갱신한다.
- 같은 원칙 안에서 화면, UseCase, Mapper, ViewModel이 단순 추가되는 경우 문서 갱신은 필수가 아니다.
- Firestore/Storage 경로, sync 정책, App Check, budget 방어 로직 변경 시 Android와 `firebase-server`를 함께 검증한다.
- 로컬 커밋은 `.gitmessage.txt` 템플릿을 따르고, GitHub Issue/PR 작성 시 `.github/` 하위 템플릿을 우선 참조한다.

## 권장 검증
- 문서 배치 확인: `find . -name AGENTS.md -print | sort`
- 오래된 패키지/모듈 경로가 문서에 남지 않았는지 리팩토링 이력에 맞춰 검색한다. 검색어 자체를 `AGENTS.md`에 남겨 false positive를 만들지 않는다.
- Gradle 모듈 변경 시: `./gradlew :app:assembleDebug`
