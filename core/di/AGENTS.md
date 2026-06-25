# core/di/AGENTS.md

> **BLUF**
> `:core:di`는 여러 모듈에서 공유하는 Hilt qualifier와 공통 DI module만 담당한다. feature binding이나 구현체 조립을 소유하지 않는다.

## 역할
- dispatcher, coroutine scope, key 등 전역적으로 재사용되는 qualifier를 제공한다.
- 공통 concurrency provider를 모듈 간 중복 없이 제공한다.
- app/data에서 같은 qualifier를 중복 선언하지 않도록 기준점을 제공한다.

## 의존성 규칙
- DI 기반 코드에 필요한 최소 의존성만 허용한다.
- feature 구현체, repository binding, Firebase/Room/DataStore provider는 소유하지 않는다.
- UI dependency provider를 두지 않는다.
- Android/Hilt module은 공통 주입 기반에 한해서만 허용한다.

## 패키지/코드 배치 규칙
- qualifier와 module은 책임이 드러나는 패키지로 구분한다.
- 같은 타입을 여러 방식으로 주입할 때는 qualifier를 명시한다.
- 새 qualifier는 실제로 여러 모듈에서 공유될 때만 추가한다.
- API key qualifier처럼 app과 data 양쪽에서 공유되는 이름은 이 모듈에만 둔다.

## 금지사항
- domain contract와 data implementation binding을 두지 않는다.
- feature별 binding을 두지 않는다.
- 공통성이 낮은 provider를 편의상 추가하지 않는다.
- Firebase/Room/DataStore 인스턴스 provider를 추가하지 않는다.

## 변경 시 체크리스트
- qualifier 추가 시 기존 이름과 충돌하지 않는지 확인한다.
- dispatcher/scope 변경 시 테스트 대체 가능성과 lifecycle 범위를 확인한다.
- app/data에 중복 qualifier가 생기지 않았는지 검색으로 검증한다.

## 권장 검증
- `./gradlew :core:di:compileDebugKotlin`
- 중복 qualifier 확인: `rg "annotation class .*Dispatcher|annotation class .*Scope|GeminiApiKey" app data core -g "*.kt"`
- DI 변경 시 `./gradlew :app:kaptDebugKotlin`
