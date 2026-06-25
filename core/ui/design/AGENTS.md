# core/ui/design/AGENTS.md

> **BLUF**
> `:core:ui:design`은 Compose design system 모듈이다. 시각적 규칙, theme, token, 재사용 component만 담당한다.

## 역할
- 앱의 theme, color, typography, preview 기준을 제공한다.
- 여러 화면에서 재사용 가능한 design system component를 제공한다.
- UI의 시각적 일관성과 dark/light 대응 기준을 관리한다.

## 의존성 규칙
- Compose design system에 필요한 의존성을 허용한다.
- `:core:common`은 design token/component에 순수 공통 타입이 필요할 때만 참조한다.
- `:domain`, `:data`, `:presentation`을 참조하지 않는다.
- UI support 책임은 `:core:ui:common`에 둔다.

## 패키지/코드 배치 규칙
- token, theme, preview, component 책임을 구분한다.
- component API는 domain 의미가 아니라 UI 표현 의미를 기준으로 설계한다.
- 공통 component는 여러 feature에서 재사용 가능한 수준일 때만 추가한다.
- skeleton/loading 같은 상태 시각화는 domain 상태 처리와 분리한다.

## 금지사항
- `LoadState`, `UiText`, domain error handling 같은 상태 처리 코드를 두지 않는다.
- feature-specific component를 두지 않는다.
- navigation, ViewModel, UseCase, Repository를 참조하지 않는다.
- data/domain model에 직접 의존하지 않는다.

## 변경 시 체크리스트
- theme/token 변경 시 주요 화면 preview와 dark/light 표시를 확인한다.
- component API 변경 시 presentation 사용처를 확인한다.
- 상태 처리나 resource helper가 필요하면 `:core:ui:common`으로 분리한다.
- feature 전용 props가 늘어나면 presentation feature 내부 component로 이동한다.

## 권장 검증
- `./gradlew :core:ui:design:compileDebugKotlin`
- 사용처 영향 확인: `./gradlew :presentation:compileDebugKotlin`
- 의존성 경계 확인: `rg "kr\\.co\\.domain|kr\\.co\\.data|kr\\.co\\.presentation" core/ui/design/src/main/java`
