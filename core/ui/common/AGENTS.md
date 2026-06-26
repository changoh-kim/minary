# core/ui/common/AGENTS.md

> **BLUF**
> `:core:ui:common`은 feature UI에서 공통으로 쓰는 UI support 모듈이다. design system이나 feature-specific UI를 소유하지 않는다.

## 역할
- 화면 상태 처리, UI text, error handling, resource helper, parceler 등 UI 지원 코드를 제공한다.
- domain/common 타입을 UI에서 표시하기 위한 최소 mapping을 제공한다.
- feature 모듈화 이후에도 공유 가능한 presentation support API를 제공한다.

## 의존성 규칙
- Android/Compose UI support에 필요한 의존성을 허용한다.
- `:core:common`을 참조할 수 있다.
- `:domain`, `:data`, `:presentation`을 참조하지 않는다.
- design token/component 책임은 `:core:ui:design`에 둔다.

## 패키지/코드 배치 규칙
- 상태 처리, 텍스트, 에러, 리소스, parceler, 표시 mapping 책임을 구분한다.
- `DomainError` 공통 helper는 UI support 범위로 제한하고, 화면 문맥별 `DomainError` 처리는 presentation ViewModel에 둔다.
- public API는 feature 화면에서 안정적으로 재사용할 수 있게 유지한다.
- UI 지원 성격과 design system 성격을 혼합하지 않는다.
- resource helper 변경 시 locale 리소스와 호출부를 함께 갱신한다.

## 금지사항
- theme, typography, color token, design system component를 추가하지 않는다.
- 특정 feature 화면 전용 component를 두지 않는다.
- ViewModel, UseCase, Repository, DataSource를 참조하지 않는다.
- domain/data 구현 로직을 포함하지 않는다.
- raw exception 또는 `Throwable`을 presentation state나 side effect로 전달하는 helper를 추가하지 않는다.

## 변경 시 체크리스트
- public API 변경 시 presentation feature import 영향을 확인한다.
- resource 변경 시 locale 리소스를 함께 갱신한다.
- design system 성격이면 `:core:ui:design`, feature 전용이면 presentation feature 내부로 보낸다.
- parceler/API 변경 시 Parcelable 사용 모델 컴파일을 검증한다.

## 권장 검증
- `./gradlew :core:ui:common:compileDebugKotlin`
- 사용처 영향 확인: `./gradlew :presentation:compileDebugKotlin`
- 의존성 경계 확인: `rg "kr\\.co\\.domain|kr\\.co\\.data|kr\\.co\\.presentation" core/ui/common/src/main/java`
