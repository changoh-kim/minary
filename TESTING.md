# Minary Testing Guide

> 이 문서는 `:core`, `:domain`, `:data`, `:presentation`, `:app`, `firebase-server`를 포함한 전체 테스트 운영 방법을 정리한다.
> 모듈별 세부 규칙은 각 모듈의 `TESTING.md`를 우선한다.

## 목적

- 로컬에서 테스트를 어떻게 재현하는지 한눈에 확인한다.
- GitHub Actions가 어떤 검증을 자동/수동으로 수행하는지 이해한다.
- 실패가 났을 때 로컬 재현과 CI 로그 확인 순서를 빠르게 잡는다.

## 전체 문서 구조

- [core/TESTING.md](./core/TESTING.md)
- [domain/TESTING.md](./domain/TESTING.md)
- [data/TESTING.md](./data/TESTING.md)
- [presentation/TESTING.md](./presentation/TESTING.md)
- [app/TESTING.md](./app/TESTING.md)
- [firebase-server/TESTING.md](./firebase-server/TESTING.md)

## 로컬 테스트 운영

### 1. Android Studio Edit Configuration

프로젝트는 GitHub Actions와 같은 흐름을 로컬에서도 재현할 수 있도록 Shell Script 기반 Run Configuration을 사용한다.

권장 설정은 다음과 같다.

- `Start Firebase Emulator Suite`
  - `Script file`
  - `Interpreter path`: `/bin/bash`
  - `Execute in the terminal`: 해제
- `CI - Android Fast Checks`
  - `Script file`
  - `Interpreter path`: `/bin/bash`
  - `Execute in the terminal`: 해제
- `CI - Android Instrumented Tests`
  - `Script file`
  - `Interpreter path`: `/bin/bash`
  - `Execute in the terminal`: 해제
- `CI - Firebase Emulator Integration`
  - `Script file`
  - `Interpreter path`: `/bin/bash`
  - `Execute in the terminal`: 해제

이 설정을 쓰는 이유는 프로젝트의 재현 스크립트가 `bash` 기준으로 작성되어 있기 때문이다.
`zsh`로 실행하면 `BASH_SOURCE[0]` 같은 bash 전용 문법이 깨질 수 있다.

### 2. 로컬 스크립트

프로젝트 루트 기준으로 아래 스크립트를 실행한다.

```bash
./scripts/ci/run-android-fast-local.sh
./scripts/ci/run-android-instrumented-local.sh
./scripts/ci/start-firebase-emulator-suite-local.sh
./scripts/ci/run-firebase-emulator-local.sh
./scripts/ci/run-firebase-emulator-integration-local.sh
```

스크립트별 역할은 다음과 같다.

- `run-android-fast-local.sh`
  - JVM unit test
  - Android test source compile
  - app assemble
  - firebase-server lint/build
- `run-android-instrumented-local.sh`
  - Android emulator 또는 실제 디바이스에서 계측 테스트 실행
- `start-firebase-emulator-suite-local.sh`
  - Firebase Emulator Suite만 수동 실행
- `run-firebase-emulator-local.sh`
  - Firebase Emulator를 전제로 Android `:data` 통합 테스트 실행
- `run-firebase-emulator-integration-local.sh`
  - functions lint/build
  - emulator start
  - emulator port wait
  - Android integration test
  - emulator 종료

### 3. 로컬 재현 기준

- 빠른 실패 확인은 `CI - Android Fast Checks`를 먼저 실행한다.
- Android UI/계측 문제는 `CI - Android Instrumented Tests`를 사용한다.
- Firebase rules/functions 계약 문제는 `CI - Firebase Emulator Integration`을 사용한다.
- 실제 디바이스와 Android Studio AVD 둘 다 사용할 수 있지만, emulator 계약 문제는 AVD가 우선이다.

## GitHub Actions 운영

현재 프로젝트의 GitHub Actions는 다음 3개 workflow로 구성한다.

- `android-fast.yml`
- `android-instrumented.yml`
- `firebase-emulator.yml`

### `android-fast.yml`

GitHub PR 생성, Merge에서 자동 실행되는 기본 검증이다.

포함 내용:

- build-logic check
- JVM unit tests
- Android test source compile
- app assemble
- firebase-server lint/build
- 경계 검사

### `android-instrumented.yml`

Android emulator 기반 계측 테스트용이다.

특징:

- `workflow_dispatch`로 수동 실행한다.
- 실제 디바이스 또는 Android Studio AVD와 같은 Android 런타임 검증에 사용한다.

### `firebase-emulator.yml`

Firebase Emulator Suite 통합 검증용이다.

특징:

- `workflow_dispatch`로 수동 실행한다.
- Auth, Firestore, Functions, Storage 계약 검증에 사용한다.
- production Firebase project를 대상으로 실행하지 않는다.

## 어디에 무엇을 적을지

- 프로젝트 공통 운영 규칙과 로컬 재현은 이 파일에 적는다.
- `:core`, `:domain`, `:data`, `:presentation`, `:app`, `firebase-server`의 세부 시나리오는 각 모듈 `TESTING.md`에 둔다.
- CI workflow 구조가 바뀌면 이 파일의 workflow 섹션과 `.github/workflows`를 함께 갱신한다.

## 실패 시 확인 순서

1. 로컬 스크립트로 같은 명령을 재현한다.
2. Android Studio Run Configuration의 interpreter가 `/bin/bash`인지 확인한다.
3. GitHub Actions 로그에서 실패한 job과 step을 확인한다.
4. Firebase 관련 실패는 emulator 로그와 `firebase-server` 문서를 함께 본다.

## 참고

- `TESTING.md`는 구현 코드가 아니라 운영 문서다.
- 테스트 자체의 상세 설계는 각 모듈 문서와 실제 test source를 우선한다.
